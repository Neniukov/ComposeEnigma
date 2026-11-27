package com.neniukov.composeenigma.ui.signal

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import com.neniukov.composeenigma.ui.theme.SignalColors as C

internal object SignalCabinet {
    const val Width = 360f
    const val Height = 412f
    const val DialX = 267f
    const val DialY = 325f
    const val DialHitRadius = 61f
}

internal class SignalArtwork(
    private val frequencyLabel: String,
    private val frequencyUnit: String,
    private val tuningLabel: String,
    private val syncLabel: String,
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val imagePaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val mono = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE }
    private val tube = RectF(33f, 53f, 327f, 249f)
    private val tubePath = Path().apply { addRoundRect(tube, 17f, 17f, Path.Direction.CW) }
    private val shell = LinearGradient(
        18f, 24f, 349f, 391f,
        intArrayOf(C.CabinetLight, C.CabinetMid, C.CabinetDark, C.CabinetBlack),
        floatArrayOf(0f, 0.32f, 0.72f, 1f),
        Shader.TileMode.CLAMP,
    )
    private val face = LinearGradient(0f, 20f, 0f, 398f, C.CabinetMid, C.CabinetDark, Shader.TileMode.CLAMP)
    private val dialFace = LinearGradient(
        235f, 289f, 296f, 363f,
        intArrayOf(C.Rim, C.CabinetMid, C.CabinetBlack), floatArrayOf(0f, 0.43f, 1f),
        Shader.TileMode.CLAMP,
    )
    private val screenLight = RadialGradient(
        170f, 152f, 169f,
        intArrayOf(0x00000000, 0x00000000, 0xC4071216.toInt()),
        floatArrayOf(0f, 0.57f, 1f), Shader.TileMode.CLAMP,
    )
    private val reflection = LinearGradient(
        27f, 39f, 296f, 195f,
        intArrayOf(0x42407767, 0x104F8C80, 0x00000000),
        floatArrayOf(0f, 0.29f, 0.68f), Shader.TileMode.CLAMP,
    )
    private val cameraLight = RadialGradient(
        FeedbackWidth * 0.54f, FeedbackHeight * 0.47f, 172f,
        intArrayOf(0xFF316657.toInt(), 0xFF193F36.toInt(), 0xFF071918.toInt()),
        floatArrayOf(0f, 0.53f, 1f), Shader.TileMode.CLAMP,
    )
    private val halo = RadialGradient(
        180f, 210f, 189f,
        intArrayOf(0x69548C75, 0x55446E58, 0), floatArrayOf(0f, 0.59f, 1f),
        Shader.TileMode.CLAMP,
    )
    private val sourceStrip = Rect()
    private val destinationStrip = RectF()
    private val strokePath = Path()
    private val noise = Random(0x435254).let { random -> FloatArray(400) { random.nextFloat() } }

    private var readFrame = newFeedbackBitmap()
    private var writeFrame = newFeedbackBitmap()
    private val feedbackCanvas = Canvas(writeFrame)
    private var lastSample: SignalFrame? = null
    private var displayFrequency = -1
    private var frequencyText = ""

    fun draw(canvas: Canvas, frame: SignalFrame, dialFocused: Boolean = false) {
        updateFeedback(frame)
        paint.shader = halo
        paint.alpha = (112f + 118f * frame.clarity).roundToInt()
        canvas.drawRect(0f, 8f, SignalCabinet.Width, 411f, paint)
        paint.shader = null
        drawConsole(canvas, frame, readFrame, dialFocused, showLabels = true)
    }

    private fun updateFeedback(frame: SignalFrame) {
        val previous = lastSample
        val restarted = previous != null && frame.time < previous.time
        if (restarted) {
            readFrame.eraseColor(C.DarkGlass)
            writeFrame.eraseColor(C.DarkGlass)
        }
        val stillChanged = frame.reducedMotion && frame != previous
        if (previous != null && !restarted && !stillChanged && frame.time - previous.time < 1f / 32f) return

        // A still frame converges off screen so it also contains the nested screens.
        repeat(if (previous == null || stillChanged || restarted) 5 else 1) {
            feedbackCanvas.setBitmap(writeFrame)
            drawCameraFrame(feedbackCanvas, frame)
            val previousRead = readFrame
            readFrame = writeFrame
            writeFrame = previousRead
        }
        lastSample = frame
    }

    private fun drawCameraFrame(canvas: Canvas, frame: SignalFrame) {
        val width = FeedbackWidth.toFloat()
        val height = FeedbackHeight.toFloat()
        paint.shader = cameraLight
        paint.alpha = 255
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null

        for (x in 12..FeedbackWidth step 13) line(canvas, x.toFloat(), 8f, x.toFloat(), height - 8f, C.Grid, .55f, 80)
        for (y in 10..FeedbackHeight step 13) line(canvas, 8f, y.toFloat(), width - 8f, y.toFloat(), C.Grid, .55f, 80)

        val glow = 0.65f + 0.35f * frame.clarity
        repeat(4) { index ->
            val side = if (index % 2 == 0) 1f else -1f
            val x = if (side > 0f) 20f else width - 20f
            val y = if (index < 2) 20f else height - 20f
            val down = if (index < 2) 1f else -1f
            line(canvas, x, y, x + side * 20f, y, C.Phosphor, 1.2f, (155f * glow).roundToInt())
            line(canvas, x, y, x, y + down * 13f, C.Phosphor, 1.2f, (155f * glow).roundToInt())
        }
        drawWaveform(canvas, frame, 29f, 83f, 60f)
        drawWaveform(canvas, frame, width - 83f, 139f, 60f)

        // Reuse the cabinet renderer. Its glass samples the completed frame, while
        // the camera is currently drawing into the other buffer.
        val cameraScale = 0.49f
        canvas.withTranslation(width * 0.5f, height * 0.50f) {
            withRotation(-3f * (1f - frame.clarity)) {
                withScale(cameraScale, cameraScale) {
                    withTranslation(-SignalCabinet.Width / 2f, -SignalCabinet.Height / 2f) {
                        drawConsole(this, frame, readFrame, dialFocused = false, showLabels = false)
                    }
                }
            }
        }

        for (index in 0..8) {
            val bar = 9f + index * 4f
            val x = 23f + index * 5.4f
            line(canvas, x, height - 33f, x, height - 33f - bar * frame.strength, C.Phosphor, 1.4f, 111)
        }
        for (index in 0..5) {
            val x = width - 61f + index * 6f
            rectangle(canvas, x, 28f, x + 3f, 31f, if (index < 4) C.Phosphor else C.AmberLight, 143)
        }
    }

    private fun drawWaveform(canvas: Canvas, frame: SignalFrame, x: Float, y: Float, width: Float) {
        line(canvas, x, y, x + width, y, C.Grid, .65f)
        strokePath.rewind()
        repeat(44) { index ->
            val fraction = index / 43f
            val envelope = sin(fraction * Math.PI).toFloat()
            val phase = fraction * 20f + frame.roll * 6.283f + frame.time * 0.8f
            val value = y + sin(phase) * 12f * envelope * (0.39f + frame.strength * 0.61f)
            if (index == 0) strokePath.moveTo(x, value) else strokePath.lineTo(x + fraction * width, value)
        }
        fill(C.Phosphor, 130)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.15f
        canvas.drawPath(strokePath, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawConsole(
        canvas: Canvas,
        frame: SignalFrame,
        picture: Bitmap,
        dialFocused: Boolean,
        showLabels: Boolean,
    ) {
        oval(canvas, 27f, 390f, 334f, 411f, Color.BLACK, 125)
        round(canvas, 35f, 389f, 85f, 402f, 4f, C.CabinetBlack)
        round(canvas, 277f, 389f, 327f, 402f, 4f, C.CabinetBlack)
        round(canvas, 9f, 22f, 351f, 395f, 22f, C.CabinetBlack)
        shadedRound(canvas, 8f, 15f, 350f, 389f, 21f, shell)
        roundStroke(canvas, 8.5f, 15.5f, 349.5f, 388.5f, 20f, C.Rim, .85f, 155)
        shadedRound(canvas, 16f, 23f, 343f, 382f, 16f, face)
        line(canvas, 20f, 270f, 339f, 270f, C.CabinetBlack, 1.4f, 220)
        line(canvas, 20f, 272f, 339f, 272f, C.Rim, .8f, 103)

        if (showLabels) {
            for (index in 0..24) {
                val y = 278f + index * 4f
                line(canvas, 25f, y, 334f, y, if (index % 2 == 0) C.Rim else C.CabinetBlack, .48f, 32)
            }
        }
        drawReceiver(canvas, frame, showLabels)
        drawDial(canvas, frame, dialFocused, showLabels)

        round(canvas, 23f, 42f, 337f, 260f, 24f, C.CabinetBlack)
        roundStroke(canvas, 23.5f, 42.5f, 336.5f, 259.5f, 23f, C.Rim, .8f, 143)
        round(canvas, 28f, 48f, 332f, 255f, 21f, Color.BLACK)
        drawTube(canvas, picture, frame)
        roundStroke(canvas, tube.left, tube.top, tube.right, tube.bottom, 17f, C.Rim, .75f, 106)

        val pilot = if (frame.locked) C.Phosphor else C.AmberLight
        circle(canvas, 320f, 32.5f, 7f, pilot, 29)
        circle(canvas, 320f, 32.5f, 3.5f, C.CabinetBlack)
        circle(canvas, 320f, 32.5f, 2.1f, pilot)
        if (showLabels) {
            repeat(2) { index ->
                val x = 26f + index * 307f
                circle(canvas, x, 365f, 3f, C.CabinetBlack, 210)
                line(canvas, x - 1.2f, 366f, x + 1.2f, 364f, C.Rim, .6f, 166)
            }
        }
    }

    private fun drawTube(canvas: Canvas, picture: Bitmap, frame: SignalFrame) {
        canvas.withClip(tubePath) {
            rectangle(canvas, tube.left, tube.top, tube.right, tube.bottom, C.DarkGlass)
            val roll = (frame.roll * FeedbackHeight).roundToInt() % FeedbackHeight
            val interference = 1f - frame.clarity
            if (interference < .015f && (roll <= 1 || roll == FeedbackHeight - 1)) {
                imagePaint.alpha = 255
                canvas.drawBitmap(picture, null, tube, imagePaint)
            } else {
                val pixelWidth = tube.width() / FeedbackWidth
                val pixelHeight = tube.height() / FeedbackHeight
                var row = 0
                while (row < FeedbackHeight) {
                    val sourceY = (row + roll) % FeedbackHeight
                    val stripHeight = minOf(14 - row % 14, FeedbackHeight - row, FeedbackHeight - sourceY)
                    val band = row / 14
                    val pulse = sin(band * 1.16f + frame.roll * 43f)
                    val tear = (pulse * 7f + if (band % 5 == 0) cos(frame.roll * 26f) * 15f else 0f) * interference
                    sourceStrip.set(0, sourceY, FeedbackWidth, sourceY + stripHeight)
                    val top = tube.top + row * pixelHeight
                    destinationStrip.set(tube.left + tear * pixelWidth, top, tube.right + tear * pixelWidth, top + stripHeight * pixelHeight + 0.35f)
                    imagePaint.alpha = 255 - (abs(pulse) * interference * 42f).roundToInt()
                    canvas.drawBitmap(picture, sourceStrip, destinationStrip, imagePaint)
                    row += stripHeight
                }
                imagePaint.alpha = 255
            }

            if (interference > .04f) {
                val bandY = tube.top + (1f - frame.roll) * tube.height()
                rectangle(canvas, tube.left, bandY - 2f, tube.right, bandY + 5f, C.DarkGlass, (164f * interference).roundToInt())
                line(canvas, tube.left, bandY - .9f, tube.right, bandY - .9f, C.RedGhost, 1f, (146f * interference).roundToInt())
                line(canvas, tube.left, bandY + 1.8f, tube.right, bandY + 1.8f, C.Phosphor, .5f, (115f * interference).roundToInt())
                val shift = (frame.roll * 120f).toInt() % 100
                for (index in 0..57) {
                    val seed = ((index + shift) % 100) * 4
                    val x = tube.left + noise[seed] * tube.width()
                    val y = tube.top + noise[seed + 1] * tube.height()
                    line(canvas, x, y, x + 2f + noise[seed + 2] * 9f, y, C.Phosphor, .55f, (interference * 79f * noise[seed + 3]).roundToInt())
                }
            }
            for (y in 55..247 step 3) line(canvas, tube.left, y.toFloat(), tube.right, y.toFloat(), Color.BLACK, .62f, 106)

            paint.shader = reflection
            paint.alpha = 255
            canvas.drawRect(tube, paint)
            paint.shader = screenLight
            canvas.drawRect(tube, paint)
            paint.shader = null
        }
    }

    private fun drawReceiver(canvas: Canvas, frame: SignalFrame, showLabels: Boolean) {
        round(canvas, 36f, 305f, 178f, 348f, 4f, Color.BLACK)
        roundStroke(canvas, 36.5f, 305.5f, 177.5f, 347.5f, 4f, C.Rim, .8f, 120)
        round(canvas, 40f, 309f, 174f, 344f, 2f, C.DarkGlass)
        val color = if (frame.locked) C.Phosphor else C.AmberLight
        val tenths = frame.frequencyTenths
        if (tenths != displayFrequency) {
            displayFrequency = tenths
            frequencyText = "${tenths / 10}.${tenths % 10}"
        }
        text(canvas, frequencyText, 48f, 336f, 24f, color)
        if (showLabels) {
            text(canvas, frequencyLabel, 36f, 295f, 8f, C.Etching)
            text(canvas, frequencyUnit, 144f, 335f, 8f, C.Etching)
            text(canvas, syncLabel, 36f, 369.3f, 7.5f, C.Etching)
        }
        repeat(9) { index ->
            val x = 65f + index * 12.1f
            val alpha = if ((index + 0.5f) / 9f < frame.strength) 255 else 51
            round(canvas, x, 362f, x + 8.5f, 368f, 1.2f, color, alpha)
        }
    }

    private fun drawDial(canvas: Canvas, frame: SignalFrame, focused: Boolean, showLabels: Boolean) {
        val x = SignalCabinet.DialX
        val y = SignalCabinet.DialY
        val lockedColor = if (frame.locked) C.Phosphor else C.AmberLight
        repeat(25) { index ->
            val position = index / 24f
            val nearCarrier = abs(position - CarrierTuning) < .071f
            val angle = (-DialSweep / 2f + DialSweep * position).radians()
            val outer = if (index % 4 == 0 || nearCarrier) 58f else 55f
            val inner = 50f
            line(
                canvas, x + sin(angle) * inner, y - cos(angle) * inner,
                x + sin(angle) * outer, y - cos(angle) * outer,
                if (nearCarrier) lockedColor else C.Etching, if (nearCarrier) 1.45f else .85f,
                if (nearCarrier) 245 else 128,
            )
        }
        if (focused) {
            fill(C.Phosphor, 222)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.6f
            canvas.drawCircle(x, y, 62f, paint)
            paint.style = Paint.Style.FILL
        }
        oval(canvas, x - 44f, y - 39f, x + 47f, y + 49f, Color.BLACK, 180)
        circle(canvas, x, y + 3f, 44f, C.CabinetBlack)
        circle(canvas, x, y, 43f, C.Rim)
        circle(canvas, x, y, 42f, C.CabinetDark)
        canvas.withRotation(-DialSweep / 2f + DialSweep * frame.tuning, x, y) {
            repeat(60) { index ->
                val angle = (index * 6f).radians()
                line(canvas, x + sin(angle) * 35f, y - cos(angle) * 35f, x + sin(angle) * 41f, y - cos(angle) * 41f,
                    if (index % 2 == 0) C.Rim else C.CabinetBlack, .83f, 144)
            }
            paint.shader = dialFace
            paint.alpha = 255
            canvas.drawCircle(x, y, 35.5f, paint)
            paint.shader = null
            circle(canvas, x, y, 25.5f, C.CabinetBlack, 93)
            round(canvas, x - 1.65f, y - 35f, x + 1.65f, y - 24f, 1.1f, lockedColor)
        }
        if (showLabels) text(canvas, tuningLabel, x - 17.5f, 377.5f, 8f, C.Etching)
    }

    private fun fill(color: Int, alpha: Int = 255) {
        paint.color = color
        paint.alpha = alpha
    }

    private fun line(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, color: Int, width: Float, alpha: Int = 255) {
        fill(color, alpha)
        paint.strokeWidth = width
        c.drawLine(x1, y1, x2, y2, paint)
    }

    private fun round(c: Canvas, l: Float, t: Float, r: Float, b: Float, radius: Float, color: Int, alpha: Int = 255) {
        fill(color, alpha)
        c.drawRoundRect(l, t, r, b, radius, radius, paint)
    }

    private fun roundStroke(c: Canvas, l: Float, t: Float, r: Float, b: Float, radius: Float, color: Int, width: Float, alpha: Int) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width
        round(c, l, t, r, b, radius, color, alpha)
        paint.style = Paint.Style.FILL
    }

    private fun shadedRound(c: Canvas, l: Float, t: Float, r: Float, b: Float, radius: Float, shader: Shader) {
        paint.shader = shader
        paint.alpha = 255
        c.drawRoundRect(l, t, r, b, radius, radius, paint)
        paint.shader = null
    }

    private fun rectangle(c: Canvas, l: Float, t: Float, r: Float, b: Float, color: Int, alpha: Int = 255) {
        fill(color, alpha)
        c.drawRect(l, t, r, b, paint)
    }

    private fun oval(c: Canvas, l: Float, t: Float, r: Float, b: Float, color: Int, alpha: Int) {
        fill(color, alpha)
        c.drawOval(l, t, r, b, paint)
    }

    private fun circle(c: Canvas, x: Float, y: Float, radius: Float, color: Int, alpha: Int = 255) {
        fill(color, alpha)
        c.drawCircle(x, y, radius, paint)
    }

    private fun text(c: Canvas, value: String, x: Float, y: Float, size: Float, color: Int) {
        mono.color = color
        mono.textSize = size
        c.drawText(value, x, y, mono)
    }

    private fun Float.radians(): Float = (this * Math.PI / 180.0).toFloat()

    private companion object {
        const val FeedbackWidth = 336
        const val FeedbackHeight = 224

        fun newFeedbackBitmap(): Bitmap =
            createBitmap(FeedbackWidth, FeedbackHeight).apply { eraseColor(C.DarkGlass) }
    }
}
