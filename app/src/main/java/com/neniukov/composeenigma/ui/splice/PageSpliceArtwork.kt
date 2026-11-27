package com.neniukov.composeenigma.ui.splice

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import com.neniukov.composeenigma.ui.theme.SpliceColors as C

internal object SpliceLayout {
    const val Width = 320f
    const val PhotoHeight = 192.5f
    const val PhotoAspect = 0.8f
    const val BandTop = 0.49f
    const val BandBottom = 0.73f

    val room = Rect(0f, 0f, 154f, PhotoHeight)
    val yard = Rect(166f, 0f, Width, PhotoHeight)
    val band = Rect(yard.left, PhotoHeight * BandTop, yard.right, PhotoHeight * BandBottom)

    fun pane(photo: Rect) = Rect(
        left = photo.left + photo.width * 0.537f,
        top = photo.top + photo.height * 0.230f,
        right = photo.left + photo.width * 0.889f,
        bottom = photo.top + photo.height * 0.582f,
    )

    fun acceptsDrop(offset: Offset, anchor: Offset): Boolean {
        val target = pane(room).inflate(12f)
        return target.contains(band.center + offset) || target.contains(anchor + offset)
    }
}

private val wholePhoto = Rect(0f, 0f, 1f, 1f)
private val stripCrop = Rect(0f, SpliceLayout.BandTop, 1f, SpliceLayout.BandBottom)
private val windowCrop = Rect(0.16f, 0.25f, 0.84f, 0.93f)
private val stitching = PathEffect.dashPathEffect(floatArrayOf(3f, 2f))

internal class PageSpliceArtwork(
    private val roomPhoto: ImageBitmap,
    private val yardPhoto: ImageBitmap,
) {
    private val winterView = yardPhoto.asAndroidBitmap().let { bitmap ->
        val left = (windowCrop.left * bitmap.width).roundToInt()
        val top = (windowCrop.top * bitmap.height).roundToInt()
        val right = (windowCrop.right * bitmap.width).roundToInt()
        val bottom = (windowCrop.bottom * bitmap.height).roundToInt()
        Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }
    private val winterPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val swingMesh = List((MeshCells + 1) * (MeshCells + 1)) { index ->
        val x = (index % (MeshCells + 1)).toFloat() / MeshCells
        val y = (index / (MeshCells + 1)).toFloat() / MeshCells
        // Keep the crossbar and the rest of the yard still while the seat shifts.
        val across = (1f - abs(x - 0.56f) / 0.115f).coerceIn(0f, 1f)
        val belowBar = ((y - 0.39f) / 0.2f).coerceIn(0f, 1f)
        val aboveGround = ((0.70f - y) / 0.11f).coerceIn(0f, 1f)
        SwingVertex(x, y, across * belowBar * aboveGround)
    }
    private val meshCoordinates = FloatArray(swingMesh.size * 2)
    private val glowBrush = Brush.radialGradient(
        listOf(C.Gold, C.Gold.copy(alpha = 0f)),
        center = Offset.Zero,
        radius = 1f,
    )
    private val snowflakes = Random(2006).let { random ->
        List(66) {
            Snowflake(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat())
        }
    }

    fun DrawScope.drawPhotos(
        load: Float,
        offset: Offset,
        lift: Float,
        snap: Float,
        isReady: Boolean,
        seconds: Float,
        anchor: Offset,
    ) = with(SpliceLayout) {
        scale(size.width / Width, size.height / PhotoHeight, Offset.Zero) {
            drawLoadingPhoto(roomPhoto, room, (load / 0.88f).coerceAtMost(1f))
            drawLoadingPhoto(yardPhoto, yard, (load * 1.25f - 0.25f).coerceIn(0f, 1f) * BandBottom)

            if (isReady) {
                val pane = pane(room)
                val hovering = lift > 0f && acceptsDrop(offset, anchor)
                val slot = pane.inflate(2f)
                val strip = band.translate(offset)

                if (snap == 0f) {
                    drawRect(
                        C.Gold.copy(alpha = if (hovering) 0.24f else 0.05f),
                        slot.topLeft,
                        slot.size,
                    )
                    drawRect(
                        C.Gold.copy(alpha = if (hovering) 1f else 0.84f),
                        slot.topLeft,
                        slot.size,
                        style = Stroke(1.15f, pathEffect = stitching),
                    )
                }

                if (lift > 0f || snap > 0f) {
                    drawRect(C.EmptyPhoto, band.topLeft, band.size)
                    drawRect(
                        C.ChromeEdge,
                        band.deflate(1f).topLeft,
                        band.deflate(1f).size,
                        style = Stroke(1f, pathEffect = stitching),
                    )
                }

                if (snap >= 1f) {
                    drawWinterPane(pane, seconds)
                } else {
                    drawStrip(lerp(strip, pane, snap), lerp(stripCrop, windowCrop, snap), lift * (1f - snap))
                }
            }
        }
    }

    fun DrawScope.drawRoom(photo: Rect, seconds: Float) {
        drawPhoto(roomPhoto, wholePhoto, photo)
        drawWinterPane(SpliceLayout.pane(photo), seconds)
        drawTreeLights(photo, seconds)
    }

    private fun DrawScope.drawLoadingPhoto(image: ImageBitmap, photo: Rect, fraction: Float) {
        val bands = 13
        val step = photo.height / bands
        repeat(bands) { index ->
            drawRect(
                if (index % 2 == 0) C.EmptyPhoto else C.Silver,
                Offset(photo.left, photo.top + index * step),
                Size(photo.width, step + 0.2f),
            )
        }
        val loadedEdge = photo.top + photo.height * fraction
        if (fraction > 0f) {
            clipRect(photo.left, photo.top, photo.right, loadedEdge) {
                drawPhoto(image, wholePhoto, photo)
            }
        }
        if (fraction < 0.99f) {
            drawLine(C.ChromeLight.copy(alpha = 0.74f), Offset(photo.left, loadedEdge), Offset(photo.right, loadedEdge))
        }
    }

    private fun DrawScope.drawStrip(strip: Rect, crop: Rect, lift: Float) {
        if (lift > 0f) {
            val shadow = strip.translate(Offset(2f, 4f) * lift).inflate(2f)
            drawRect(C.Night.copy(alpha = 0.45f * lift), shadow.topLeft, shadow.size)
        }

        val columns = if (lift > 0.001f) 20 else 1
        repeat(columns) { index ->
            val start = index.toFloat() / columns
            val end = (index + 1f) / columns
            val sag = sin((start + end) * (Math.PI / 2)).toFloat() * lift * 4.5f
            val left = strip.left + strip.width * start
            val right = strip.left + strip.width * end
            val sample = Rect(crop.left + crop.width * start, crop.top, crop.left + crop.width * end, crop.bottom)
            drawPhoto(yardPhoto, sample, Rect(left, strip.top + sag, right, strip.bottom + sag))
            drawLine(C.Gold, Offset(left, strip.top + sag), Offset(right, strip.top + sag), 1.1f)
            drawLine(C.Gold, Offset(left, strip.bottom + sag), Offset(right, strip.bottom + sag), 1.1f)
        }

        if (lift == 0f) {
            val grip = Rect(strip.right - 23f, strip.center.y - 8f, strip.right - 4f, strip.center.y + 8f)
            drawRect(C.Night.copy(alpha = 0.76f), grip.topLeft, grip.size)
            repeat(3) { index ->
                val y = grip.top + 4f + 4f * index
                drawLine(C.Snow, Offset(grip.left + 5f, y), Offset(grip.right - 5f, y), 1f)
            }
        }
    }

    private fun DrawScope.drawWinterPane(pane: Rect, seconds: Float) {
        clipRect(pane.left, pane.top, pane.right, pane.bottom) {
            drawSwingingYard(pane, seconds)
            drawRect(C.BlueDark.copy(alpha = 0.12f), pane.topLeft, pane.size)
            val intensity = (seconds * 2f).coerceIn(0f, 1f)
            snowflakes.forEach { flake ->
                val drift = sin(seconds * (0.65f + flake.depth) + flake.phase * 6.28f) * 0.029f
                val x = (flake.x + drift + 1f) % 1f
                val y = (flake.y + seconds * (0.12f + 0.19f * flake.depth)) % 1f
                val radius = pane.width * (0.002f + 0.008f * flake.depth)
                drawCircle(
                    C.Snow.copy(alpha = (0.37f + 0.56f * flake.depth) * intensity),
                    radius,
                    Offset(pane.left + x * pane.width, pane.top + y * pane.height),
                )
            }

            repeat(5) { index ->
                val flicker = 0.55f + 0.45f * sin(seconds * (1.7f + index * 0.35f) + index)
                val point = Offset(pane.left + pane.width * (0.07f + index * 0.065f), pane.top + pane.height * (0.27f + (index % 3) * 0.19f))
                drawGlow(point, pane.width * (0.035f + 0.006f * index), intensity * flicker * 0.42f)
            }
            drawLine(
                C.Snow.copy(alpha = 0.09f),
                Offset(pane.left + pane.width * 0.07f, pane.bottom),
                Offset(pane.left + pane.width * 0.39f, pane.top),
                pane.width * 0.026f,
            )
            drawRect(C.Night.copy(alpha = 0.52f), pane.topLeft, pane.size, style = Stroke(pane.width * 0.025f))
        }
    }

    private fun DrawScope.drawSwingingYard(pane: Rect, seconds: Float) {
        val sway = sin(seconds * 2.5f) * 0.024f * pane.width
        swingMesh.forEachIndexed { index, point ->
            meshCoordinates[index * 2] = pane.left + point.x * pane.width + sway * point.weight
            meshCoordinates[index * 2 + 1] = pane.top + point.y * pane.height
        }
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawBitmapMesh(winterView, MeshCells, MeshCells, meshCoordinates, 0, null, 0, winterPaint)
        }
    }

    private fun DrawScope.drawTreeLights(photo: Rect, seconds: Float) {
        treeLights.forEachIndexed { index, point ->
            val glow = 0.5f + 0.5f * sin(seconds * (2.7f + index * 0.29f) + index)
            val center = Offset(photo.left + point.x * photo.width, photo.top + point.y * photo.height)
            drawGlow(center, photo.width * 0.021f, glow * 0.54f)
        }
    }

    private fun DrawScope.drawGlow(center: Offset, radius: Float, intensity: Float) {
        if (intensity <= 0f) return
        translate(center.x, center.y) {
            scale(radius, pivot = Offset.Zero) {
                drawCircle(glowBrush, radius = 1f, center = Offset.Zero, alpha = intensity)
            }
        }
    }

    private fun DrawScope.drawPhoto(image: ImageBitmap, source: Rect, target: Rect) {
        val x = (image.width * source.left).roundToInt()
        val y = (image.height * source.top).roundToInt()
        val right = (image.width * source.right).roundToInt()
        val bottom = (image.height * source.bottom).roundToInt()
        val leftPixel = target.left.roundToInt()
        val topPixel = target.top.roundToInt()
        drawImage(
            image = image,
            srcOffset = IntOffset(x, y),
            srcSize = IntSize(right - x, bottom - y),
            dstOffset = IntOffset(leftPixel, topPixel),
            dstSize = IntSize(target.right.roundToInt() - leftPixel, target.bottom.roundToInt() - topPixel),
        )
    }

    private data class SwingVertex(val x: Float, val y: Float, val weight: Float)

    private data class Snowflake(val x: Float, val y: Float, val depth: Float, val phase: Float)

    private companion object {
        const val MeshCells = 24
        val treeLights = listOf(
            Offset(0.224f, 0.287f), Offset(0.262f, 0.334f), Offset(0.168f, 0.391f),
            Offset(0.247f, 0.474f), Offset(0.345f, 0.542f), Offset(0.361f, 0.593f),
            Offset(0.337f, 0.662f), Offset(0.124f, 0.771f), Offset(0.243f, 0.744f),
        )
    }
}
