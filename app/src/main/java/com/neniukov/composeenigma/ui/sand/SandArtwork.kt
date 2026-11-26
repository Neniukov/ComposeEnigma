package com.neniukov.composeenigma.ui.sand

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

internal const val SAND_STAGE = 800
private const val CENTER = SAND_STAGE / 2f
private const val BED_RADIUS = 319f
private val StageBounds = RectF(0f, 0f, SAND_STAGE.toFloat(), SAND_STAGE.toFloat())
private val FilteredBitmap = Paint(Paint.FILTER_BITMAP_FLAG)

internal fun Offset.toSandPosition(): Offset = (this - Offset(CENTER, CENTER)) / BED_RADIUS

internal fun Float.toSandDistance(): Float = this / BED_RADIUS

private fun Offset.toStage(): Offset = this * BED_RADIUS + Offset(CENTER, CENTER)

internal interface SandTrail {
    fun start(position: Offset)
    fun extend(position: Offset)
    fun clear(position: Offset)
    fun draw(canvas: Canvas)
}

internal class CanvasSandTrail : SandTrail {
    private val path = Path()
    private var imprint: Bitmap? = null
    private var imprintCanvas: Canvas? = null
    private var segments = 0

    private val softEdge = groovePaint(0x49351F0E, 8f)
    private val ridge = groovePaint(0xC6D8A46D.toInt(), 3.2f)
    private val furrow = groovePaint(0xF2372319.toInt(), 2.45f)

    override fun start(position: Offset) {
        val point = position.toStage()
        path.moveTo(point.x, point.y)
    }

    override fun extend(position: Offset) {
        val point = position.toStage()
        path.lineTo(point.x, point.y)
        segments++
        if (segments == 720) {
            val target = imprintCanvas ?: createImprint()
            drawGrooves(target)
            path.rewind()
            path.moveTo(point.x, point.y)
            segments = 0
        }
    }

    override fun clear(position: Offset) {
        imprint?.eraseColor(Color.TRANSPARENT)
        path.rewind()
        segments = 0
        start(position)
    }

    override fun draw(canvas: Canvas) {
        imprint?.let { canvas.drawBitmap(it, null, StageBounds, FilteredBitmap) }
        drawGrooves(canvas)
    }

    private fun drawGrooves(canvas: Canvas) {
        if (segments == 0) return
        canvas.drawPath(path, softEdge)
        val saved = canvas.save()
        canvas.translate(-0.7f, -1.7f)
        canvas.drawPath(path, ridge)
        canvas.restoreToCount(saved)
        canvas.drawPath(path, furrow)
    }

    private fun createImprint(): Canvas {
        val bitmap = Bitmap.createBitmap(SAND_STAGE, SAND_STAGE, Bitmap.Config.ARGB_8888)
        imprint = bitmap
        return Canvas(bitmap).also { imprintCanvas = it }
    }

    private fun groovePaint(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
}

internal class SandArtwork {
    private val sweep by lazy { SandSweep(Tray, CENTER, BED_RADIUS) }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val brass = RadialGradient(
        -5f, -9f, 29f,
        intArrayOf(0xFFFFE6B0.toInt(), 0xFFE7AC66.toInt(), 0xFF86502B.toInt(), 0xFF392317.toInt()),
        floatArrayOf(0f, 0.37f, 0.76f, 1f),
        Shader.TileMode.CLAMP,
    )
    private val shadow = RadialGradient(
        11f, 16f, 33f, 0xC918110E.toInt(), Color.TRANSPARENT, Shader.TileMode.CLAMP,
    )
    private val wire = LinearGradient(
        0f, 40f, 0f, 650f,
        intArrayOf(0xFFD1AD79.toInt(), 0x998D735B.toInt(), 0xB2D8B287.toInt()),
        null,
        Shader.TileMode.CLAMP,
    )

    fun drawTray(canvas: Canvas) {
        canvas.drawBitmap(Tray, null, StageBounds, FilteredBitmap)
    }

    fun drawSweep(canvas: Canvas, progress: Float) {
        sweep.draw(canvas, progress)
    }

    fun drawStylus(canvas: Canvas, position: Offset, lift: Float) {
        val point = position.toStage()
        val anchorX = 328f
        val anchorY = 60f
        val top = point.y - 6f * lift

        paint.color = 0x76302319
        paint.strokeWidth = 2.7f
        canvas.drawLine(anchorX + 2f, anchorY + 4f, point.x + 11f, point.y + 16f, paint)
        paint.color = Color.WHITE
        paint.shader = wire
        paint.strokeWidth = 1.6f
        canvas.drawLine(anchorX, anchorY, point.x, top, paint)
        paint.shader = null

        val saved = canvas.save()
        canvas.translate(point.x, top)
        paint.shader = shadow
        canvas.drawCircle(11f, 16f, 33f, paint)
        paint.shader = null
        paint.color = 0xFF36231A.toInt()
        canvas.drawCircle(0f, 3f, 20f, paint)
        paint.color = Color.WHITE
        paint.shader = brass
        canvas.drawCircle(0f, 0f, 19f, paint)
        paint.shader = null
        paint.color = 0x735E371F
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.9f
        canvas.drawCircle(0f, 0f, 12.6f, paint)
        canvas.drawCircle(0f, 0f, 7.5f, paint)
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFCE5AD.toInt()
        canvas.drawCircle(-3f, -4f, 2.4f, paint)
        canvas.restoreToCount(saved)

        paint.color = 0xFF2B1C16.toInt()
        canvas.drawCircle(anchorX, anchorY, 8f, paint)
        paint.color = 0xFFB38652.toInt()
        canvas.drawCircle(anchorX, anchorY, 4.4f, paint)
        paint.color = 0xFFE7CA92.toInt()
        canvas.drawCircle(anchorX - 1f, anchorY - 1f, 1.3f, paint)
    }

    companion object {
        private const val COVER_SIZE = 256
        private const val COVER_SAMPLES_PER_SECOND = 30
        private val Tray: Bitmap by lazy { createTray() }

        val cover: ImageBitmap by lazy { createCover().asImageBitmap() }

        private fun createCover(): Bitmap {
            val artwork = SandArtwork()
            val physics = SandPhysics()
            val trail = CanvasSandTrail()
            trail.start(physics.position)
            repeat(SAND_OPENING_SECONDS * COVER_SAMPLES_PER_SECOND) {
                physics.step(1f / COVER_SAMPLES_PER_SECOND)
                trail.extend(physics.position)
            }

            val bitmap = createBitmap(COVER_SIZE, COVER_SIZE)
            val canvas = Canvas(bitmap)
            val scale = COVER_SIZE.toFloat() / SAND_STAGE
            canvas.scale(scale, scale)
            artwork.drawTray(canvas)
            trail.draw(canvas)
            artwork.drawStylus(canvas, physics.position, lift = 0f)
            return bitmap
        }

        private fun createTray(): Bitmap {
            val bitmap = Bitmap.createBitmap(SAND_STAGE, SAND_STAGE, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            paint.shader = RadialGradient(
                CENTER, CENTER + 12f, 400f, 0xD5000000.toInt(), Color.TRANSPARENT, Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(CENTER, CENTER + 12f, 400f, paint)
            paint.shader = LinearGradient(
                125f, 48f, 648f, 744f,
                intArrayOf(0xFF69523B.toInt(), 0xFF2C2019.toInt(), 0xFF493425.toInt(), 0xFF130F0D.toInt()),
                floatArrayOf(0f, 0.43f, 0.71f, 1f),
                Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(CENTER, CENTER, 370f, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            ring(canvas, paint, 369f, 1.1f, 0xB68F7351.toInt())
            ring(canvas, paint, 363f, 3.3f, 0xFF241A15.toInt())
            ring(canvas, paint, 355f, 1.1f, 0x657F6445)
            ring(canvas, paint, 348f, 1.6f, 0xFF9F7042.toInt())
            ring(canvas, paint, 345f, 0.8f, 0xD3DDB478.toInt())
            ring(canvas, paint, 337f, 4.6f, 0xFF191311.toInt())
            ring(canvas, paint, 328f, 1.4f, 0xCBB99061.toInt())

            paint.strokeWidth = 1.3f
            repeat(96) { index ->
                val angle = index * (2 * Math.PI / 96).toFloat()
                val x = cos(angle)
                val y = sin(angle)
                val outer = if (index % 8 == 0) 358f else 354f
                paint.color = if (index % 8 == 0) 0xFFC19C6B.toInt() else 0x818C694A.toInt()
                canvas.drawLine(CENTER + x * 350f, CENTER + y * 350f, CENTER + x * outer, CENTER + y * outer, paint)
            }

            paint.style = Paint.Style.FILL
            paint.shader = RadialGradient(
                CENTER - 94f, CENTER - 153f, 598f,
                intArrayOf(0xFF836044.toInt(), 0xFF65482F.toInt(), 0xFF3B291F.toInt()),
                floatArrayOf(0f, 0.52f, 1f),
                Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(CENTER, CENTER, BED_RADIUS + 5f, paint)
            paint.shader = null
            grain(canvas, paint)
            paint.style = Paint.Style.STROKE
            ring(canvas, paint, BED_RADIUS + 3f, 3f, 0xA42D211B.toInt())
            ring(canvas, paint, BED_RADIUS - 1f, 0.9f, 0x5ED3A879)
            return bitmap
        }

        private fun ring(canvas: Canvas, paint: Paint, radius: Float, width: Float, color: Int) {
            paint.color = color
            paint.strokeWidth = width
            canvas.drawCircle(CENTER, CENTER, radius, paint)
        }

        private fun grain(canvas: Canvas, paint: Paint) {
            val random = Random(28)
            repeat(28000) { index ->
                val angle = random.nextFloat() * (2 * Math.PI).toFloat()
                val radius = sqrt(random.nextFloat()) * (BED_RADIUS + 2f)
                val x = CENTER + cos(angle) * radius
                val y = CENTER + sin(angle) * radius
                val alpha = random.nextInt(12, 100)
                paint.color = if (index % 3 == 0) Color.argb(alpha, 235, 188, 127) else Color.argb(alpha, 51, 31, 23)
                canvas.drawCircle(x, y, 0.26f + random.nextFloat() * 1.02f, paint)
            }
        }
    }
}
