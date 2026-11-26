package com.neniukov.composeenigma.ui.sand

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

internal class SandSweep(tray: Bitmap, center: Float, bedRadius: Float) {
    private val top = center - bedRadius
    private val bottom = center + bedRadius
    private val bed = Path().apply { addCircle(center, center, bedRadius, Path.Direction.CW) }

    private val mask = LinearGradient(0f, 0f, 0f, FEATHER, Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP)
    private val maskPosition = Matrix()
    private val cleanBed = Paint().apply {
        shader = ComposeShader(
            BitmapShader(tray, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP),
            mask,
            PorterDuff.Mode.DST_IN,
        )
    }
    private val crest = Paint().apply {
        shader = LinearGradient(
            0f, -46f, 0f, FEATHER + 20f,
            intArrayOf(Color.TRANSPARENT, 0x248A562F, 0x55D8A36B, 0x2BBE8854, Color.TRANSPARENT),
            floatArrayOf(0f, 0.29f, 0.54f, 0.72f, 1f),
            Shader.TileMode.CLAMP,
        )
    }
    private val grainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFEBC291.toInt() }
    private val grains = Random(72).let { random ->
        List(190) {
            Grain(
                x = random.nextFloat() * SAND_STAGE,
                offset = -34f + random.nextFloat() * 89f,
                radius = 0.55f + random.nextFloat() * 1.5f,
                alpha = 22 + random.nextInt(86),
                drift = (random.nextFloat() - 0.5f) * 26f,
            )
        }
    }

    fun draw(canvas: Canvas, progress: Float) {
        val eased = progress * progress * (3f - 2f * progress)
        val line = top - FEATHER - 20f + (bottom - top + FEATHER + 66f) * eased
        val saved = canvas.save()
        canvas.clipPath(bed)

        maskPosition.setTranslate(0f, line)
        mask.setLocalMatrix(maskPosition)
        val covered = (line + FEATHER).coerceIn(top, bottom)
        canvas.drawRect(0f, top, SAND_STAGE.toFloat(), covered, cleanBed)

        canvas.translate(0f, line)
        canvas.drawRect(0f, -46f, SAND_STAGE.toFloat(), FEATHER + 20f, crest)
        val visibility = sin(progress * PI.toFloat()).coerceAtLeast(0f)
        grains.forEach { grain ->
            grainPaint.alpha = (grain.alpha * visibility).toInt()
            canvas.drawCircle(grain.x + grain.drift * eased, grain.offset, grain.radius, grainPaint)
        }
        canvas.restoreToCount(saved)
    }

    private data class Grain(val x: Float, val offset: Float, val radius: Float, val alpha: Int, val drift: Float)

    private companion object {
        const val FEATHER = 54f
    }
}
