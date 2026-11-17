package com.neniukov.composeenigma.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

internal enum class Glyph {
    Arrow,
    Back,
    Heart,
    HeartFilled,
    Play,
    Pause,
    Compass,
    Check,
}

@Composable
internal fun GlyphView(
    kind: Glyph,
    color: Color,
    modifier: Modifier,
) {
    Canvas(modifier) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            val stroke = Stroke(1.5f, cap = StrokeCap.Round)
            when (kind) {
                Glyph.Arrow -> {
                    drawLine(color, Offset(4f, 12f), Offset(20f, 12f), 1.5f, StrokeCap.Round)
                    drawPath(
                        Path().apply {
                            moveTo(14f, 6f)
                            lineTo(20f, 12f)
                            lineTo(14f, 18f)
                        },
                        color,
                        style = stroke,
                    )
                }
                Glyph.Back -> {
                    drawLine(color, Offset(4f, 12f), Offset(20f, 12f), 1.5f, StrokeCap.Round)
                    drawPath(
                        Path().apply {
                            moveTo(10f, 6f)
                            lineTo(4f, 12f)
                            lineTo(10f, 18f)
                        },
                        color,
                        style = stroke,
                    )
                }
                Glyph.Heart, Glyph.HeartFilled -> {
                    val heart = Path().apply {
                        moveTo(12f, 20f)
                        cubicTo(8f, 17f, 2.8f, 13.5f, 2.8f, 8.4f)
                        cubicTo(2.8f, 3.5f, 8.8f, 2.5f, 12f, 6.8f)
                        cubicTo(15.2f, 2.5f, 21.2f, 3.5f, 21.2f, 8.4f)
                        cubicTo(21.2f, 13.5f, 16f, 17f, 12f, 20f)
                        close()
                    }
                    if (kind == Glyph.HeartFilled) {
                        drawPath(heart, color)
                    } else {
                        drawPath(heart, color, style = stroke)
                    }
                }
                Glyph.Play -> drawPath(
                    Path().apply {
                        moveTo(7f, 4f)
                        lineTo(20f, 12f)
                        lineTo(7f, 20f)
                        close()
                    },
                    color,
                )
                Glyph.Pause -> {
                    drawLine(color, Offset(8f, 5f), Offset(8f, 19f), 3f, StrokeCap.Round)
                    drawLine(color, Offset(16f, 5f), Offset(16f, 19f), 3f, StrokeCap.Round)
                }
                Glyph.Compass -> {
                    drawCircle(color, 10f, Offset(12f, 12f), style = Stroke(.8f))
                    drawPath(
                        Path().apply {
                            moveTo(16f, 7f)
                            lineTo(14f, 14f)
                            lineTo(8f, 17f)
                            lineTo(10f, 10f)
                            close()
                        },
                        color,
                        style = Stroke(.9f),
                    )
                    drawCircle(color, 1.2f, Offset(12f, 12f))
                }
                Glyph.Check -> drawPath(
                    Path().apply {
                        moveTo(5f, 12f)
                        lineTo(10f, 17f)
                        lineTo(20f, 7f)
                    },
                    color,
                    style = stroke,
                )
            }
        }
    }
}
