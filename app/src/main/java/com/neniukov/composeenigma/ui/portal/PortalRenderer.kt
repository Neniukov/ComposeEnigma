package com.neniukov.composeenigma.ui.portal

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.graphics.SweepGradient
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import com.neniukov.composeenigma.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import com.neniukov.composeenigma.ui.theme.PortalColors as C

private interface PortalRenderer {
    fun DrawScope.render(motion: PortalMotion)
}

@Composable
internal fun PortalCanvas(motion: PortalMotion, modifier: Modifier = Modifier) {
    val renderer = rememberPortalRenderer()
    Canvas(modifier) {
        if (renderer == null) {
            drawRect(C.Background)
        } else {
            with(renderer) { render(motion) }
        }
    }
}

@Composable
internal fun LiquidPortalCover(modifier: Modifier = Modifier) {
    val motion = remember {
        PortalMotion(initialOpening = 1f).apply {
            target = Offset(0.64f, 0.48f)
        }
    }
    PortalCanvas(motion, modifier)
}

@Composable
private fun rememberPortalRenderer(): PortalRenderer? {
    val resources = LocalResources.current
    if (LocalInspectionMode.current) {
        return remember(resources) { createPortalRenderer(resources) }
    }
    val renderer by produceState<PortalRenderer?>(initialValue = null, resources) {
        value = withContext(Dispatchers.IO) {
            createPortalRenderer(resources)
        }
    }
    return renderer
}

private fun createPortalRenderer(resources: Resources): PortalRenderer {
    val mountains = BitmapFactory.decodeResource(resources, R.drawable.dolomites)
    val desert = BitmapFactory.decodeResource(resources, R.drawable.portal_desert)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val source = resources.openRawResource(R.raw.liquid_portal).bufferedReader().use { it.readText() }
        ShaderPortalRenderer(mountains, desert, source)
    } else {
        CanvasPortalRenderer(mountains, desert)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class ShaderPortalRenderer(mountains: Bitmap, desert: Bitmap, source: String) : PortalRenderer {
    private val shader = RuntimeShader(source).apply {
        setInputShader("mountains", BitmapShader(mountains, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
        setInputShader("desert", BitmapShader(desert, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
        setFloatUniform("mountainSize", mountains.width.toFloat(), mountains.height.toFloat())
        setFloatUniform("desertSize", desert.width.toFloat(), desert.height.toFloat())
        setColorUniform("nightTint", C.NightTint.toArgb())
        setColorUniform("rimTint", C.White.toArgb())
    }
    private val brush = ShaderBrush(shader)

    override fun DrawScope.render(motion: PortalMotion) {
        val center = motion.center.value
        val drag = motion.target - center
        shader.setFloatUniform("resolution", size.width, size.height)
        shader.setFloatUniform("center", center.x, center.y)
        shader.setFloatUniform("drag", drag.x, drag.y)
        shader.setFloatUniform("opening", motion.opening.value.coerceAtLeast(0f))
        shader.setFloatUniform("time", motion.time)
        shader.setFloatUniform("rippleOrigin", motion.rippleOrigin.x, motion.rippleOrigin.y)
        shader.setFloatUniform("ripple", motion.ripple.value)
        drawRect(brush)
    }
}

private class CanvasPortalRenderer(private val mountains: Bitmap, private val desert: Bitmap) : PortalRenderer {
    private val mountainShader = BitmapShader(mountains, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    private val desertShader = BitmapShader(desert, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    private val mountainBrush = ShaderBrush(mountainShader)
    private val desertBrush = ShaderBrush(desertShader)
    private val spectrum = SweepGradient(0f, 0f, C.Spectrum.map { it.toArgb() }.toIntArray(), null)
    private val rimBrush = ShaderBrush(spectrum)
    private val matrix = Matrix()
    private val path = Path()
    private var previousSize = Size.Zero
    private val nightFilter = ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0.2126f * C.NightTint.red, 0.7152f * C.NightTint.red, 0.0722f * C.NightTint.red, 0f, 0f,
                0.2126f * C.NightTint.green, 0.7152f * C.NightTint.green, 0.0722f * C.NightTint.green, 0f, 0f,
                0.2126f * C.NightTint.blue, 0.7152f * C.NightTint.blue, 0.0722f * C.NightTint.blue, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )

    override fun DrawScope.render(motion: PortalMotion) {
        if (previousSize != size) {
            fit(mountainShader, mountains, size)
            previousSize = size
        }
        drawRect(mountainBrush, colorFilter = nightFilter)
        val center = Offset(motion.center.value.x * size.width, motion.center.value.y * size.height)
        val unit = min(size.width, size.height)
        val opening = motion.opening.value.coerceAtLeast(0f)
        if (opening > 0.001f) {
            fitDesert(size, center)
            val radius = unit * 0.335f * opening.pow(0.72f)
            val delta = motion.target - motion.center.value
            val lag = Offset(delta.x * size.width / unit, delta.y * size.height / unit)
            val stretch = (lag.getDistance() * 2.4f).coerceAtMost(0.32f)
            val direction = if (lag.getDistance() > 0f) lag / lag.getDistance() else Offset(1f, 0f)
            path.reset()
            repeat(97) { index ->
                val angle = (index / 96f) * 2f * PI.toFloat()
                val wave = (
                    sin(angle * 3f + motion.time * 1.15f) * 0.012f +
                        sin(angle * 5f - motion.time * 1.45f) * 0.006f +
                        sin(angle * 2f - motion.time * 0.7f) * 0.008f
                    ) * unit * opening
                val local = Offset(cos(angle), sin(angle)) * (radius + wave)
                val projection = local.x * direction.x + local.y * direction.y
                val point = center + local + direction * (projection * stretch / (1f - stretch))
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            path.close()
            clipPath(path) { drawRect(desertBrush) }
            matrix.setTranslate(center.x, center.y)
            spectrum.setLocalMatrix(matrix)
            drawPath(path, rimBrush, alpha = 0.13f, style = Stroke(unit * 0.055f))
            drawPath(path, rimBrush, alpha = 0.7f, style = Stroke(unit * 0.014f))
            drawPath(path, C.White, alpha = 0.8f, style = Stroke(unit * 0.0025f))
        }
        val ripple = motion.ripple.value
        if (ripple < 1f) {
            drawCircle(
                C.Cyan,
                radius = unit * (0.04f + ripple * 1.35f),
                center = Offset(motion.rippleOrigin.x * size.width, motion.rippleOrigin.y * size.height),
                alpha = (1f - ripple) * 0.28f,
                style = Stroke(1.2f.dp.toPx()),
            )
        }
    }

    private fun fit(shader: Shader, bitmap: Bitmap, size: Size) {
        val scale = max(size.width / bitmap.width, size.height / bitmap.height)
        matrix.setScale(scale, scale)
        matrix.postTranslate((size.width - bitmap.width * scale) * 0.5f, (size.height - bitmap.height * scale) * 0.5f)
        shader.setLocalMatrix(matrix)
    }

    private fun fitDesert(size: Size, center: Offset) {
        val lensScale = 0.88f
        val parallax = 0.14f
        val scale = max(size.width / desert.width, size.height / desert.height) / lensScale
        val offset = (center - Offset(size.width * 0.5f, size.height * 0.5f)) * (1f - parallax / lensScale)
        matrix.setScale(scale, scale)
        matrix.postTranslate(
            (size.width - desert.width * scale) * 0.5f + offset.x,
            (size.height - desert.height * scale) * 0.5f + offset.y,
        )
        desertShader.setLocalMatrix(matrix)
    }
}
