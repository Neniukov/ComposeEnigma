package com.neniukov.composeenigma.ui.vinyl

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.neniukov.composeenigma.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.neniukov.composeenigma.ui.theme.VinylColors as C

internal fun Modifier.vinylPaper(): Modifier = drawWithCache {
    val flecks = List(1_400) { index ->
        Offset(((index * 73) % 997) / 997f * size.width, ((index * 193) % 991) / 991f * size.height)
    }
    onDrawBehind {
        drawRect(C.Paper)
        flecks.forEachIndexed { index, point ->
            drawCircle(C.Shadow.copy(alpha = if (index % 3 == 0) 0.045f else 0.025f), 0.6f, point)
        }
    }
}

@Composable
internal fun VinylDeck(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        normalized {
            repeat(14) { index ->
                val spread = index * 2f
                drawRoundRect(
                    C.Shadow.copy(alpha = 0.006f),
                    Offset(30f - spread, 250f - spread + 14f),
                    Size(940f + spread * 2, 660f + spread * 2),
                    CornerRadius(42f + spread),
                )
            }
            drawRoundRect(C.DeckEdge, Offset(30f, 240f), Size(940f, 680f), CornerRadius(38f))
            drawRoundRect(
                Brush.linearGradient(listOf(C.DeckLight, C.Deck, C.DeckEdge), Offset.Zero, Offset(900f, 1_100f)),
                Offset(30f, 230f),
                Size(940f, 676f),
                CornerRadius(38f),
            )
            drawRoundRect(
                C.DeckLight.copy(alpha = 0.8f),
                Offset(33f, 233f),
                Size(934f, 668f),
                CornerRadius(36f),
                style = Stroke(2f),
            )
            repeat(65) { index ->
                val y = 250f + index * 9.6f
                drawLine(C.Ink.copy(alpha = 0.018f), Offset(55f, y), Offset(946f, y), 0.7f)
            }
            val center = Offset(410f, 575f)
            drawCircle(C.Shadow.copy(alpha = 0.13f), 323f, center + Offset(0f, 7f))
            drawCircle(Brush.radialGradient(listOf(C.MetalDark, C.MetalLight, C.MetalDark), center, 322f), 322f, center)
            drawCircle(C.MetalDark, 314f, center)
            repeat(96) { index ->
                val angle = index * PI * 2 / 96
                val start = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * 316f
                val end = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * 320f
                drawLine(C.MetalLight.copy(alpha = 0.65f), start, end, 1.7f)
            }
            listOf(Offset(76f, 273f), Offset(924f, 273f), Offset(924f, 860f)).forEach { screw ->
                drawCircle(C.DeckEdge, 7f, screw)
                drawLine(C.MetalDark, screw - Offset(3f, -2f), screw + Offset(3f, -2f), 1.4f)
            }
            drawRoundRect(C.DeckEdge, Offset(763f, 795f), Size(113f, 48f), CornerRadius(12f))
            drawRoundRect(C.DeckLight, Offset(767f, 797f), Size(105f, 41f), CornerRadius(10f))
            drawLine(C.MetalDark, Offset(795f, 817f), Offset(841f, 817f), 3f, StrokeCap.Round)
            drawCircle(C.MetalDark, 18f, Offset(100f, 851f))
            drawCircle(C.Accent, 11f, Offset(100f, 851f))
            drawCircle(C.Cream.copy(alpha = 0.65f), 3.5f, Offset(97f, 847f))
            repeat(4) { index ->
                drawLine(C.MetalDark.copy(alpha = 0.45f), Offset(148f + index * 13f, 844f), Offset(148f + index * 13f, 858f), 2f)
            }
        }
    }
}

@Composable
internal fun VinylRecord(angle: () -> Float, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val label = stringResource(R.string.vinyl_record_title)
    val side = stringResource(R.string.vinyl_label_side)
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            drawCircle(C.Record, radius)
            drawCircle(
                Brush.sweepGradient(
                    listOf(C.Record, C.Groove, C.Record, C.Record, C.Shine, C.Record, C.Record),
                    center,
                ),
                radius * 0.99f,
            )
            repeat(90) { index ->
                val groove = radius * (0.365f + index * 0.00685f)
                drawCircle(
                    if (index % 5 == 0) C.Record.copy(alpha = 0.8f) else C.Groove.copy(alpha = 0.5f),
                    groove,
                    style = Stroke((radius * 0.0022f).coerceAtLeast(0.45f)),
                )
            }
            drawCircle(C.Shine.copy(alpha = 0.4f), radius * 0.992f, style = Stroke(radius * 0.003f))
            drawCircle(C.Record, radius * 0.355f)
        }
        Spacer(
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = angle() }
                .drawWithCache {
                    val title = measurer.measure(
                        label,
                        TextStyle(
                            color = C.Cream,
                            fontFamily = FontFamily.Serif,
                            fontSize = (size.width * 0.055f).toSp(),
                        ),
                    )
                    val sideLabel = measurer.measure(
                        side,
                        TextStyle(
                            color = C.Cream,
                            fontFamily = FontFamily.Monospace,
                            fontSize = (size.width * 0.021f).toSp(),
                            letterSpacing = (size.width * 0.004f).toSp(),
                        ),
                    )
                    onDrawBehind {
                        val radius = size.minDimension / 2f
                        drawCircle(Brush.linearGradient(listOf(C.Label, C.LabelDark)), radius * 0.337f)
                        drawCircle(C.Cream.copy(alpha = 0.5f), radius * 0.306f, style = Stroke(radius * 0.002f))
                        drawText(title, topLeft = Offset(center.x - title.size.width / 2f, center.y - radius * 0.18f))
                        drawText(sideLabel, topLeft = Offset(center.x - sideLabel.size.width / 2f, center.y + radius * 0.18f))
                        drawCircle(C.Record, radius * 0.034f)
                        drawCircle(C.MetalLight, radius * 0.014f)
                        drawArc(C.Shine.copy(alpha = 0.4f), 24f, 11f, false, style = Stroke(radius * 0.004f))
                    }
                },
        )
    }
}

@Composable
internal fun VinylSleeve(modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val title = stringResource(R.string.vinyl_label_title)
    val edition = stringResource(R.string.vinyl_sleeve_edition)
    Spacer(
        modifier.drawWithCache {
            val titleLayout = measurer.measure(
                title,
                TextStyle(
                    color = C.SleeveDark,
                    fontFamily = FontFamily.Serif,
                    fontSize = (size.width * 0.16f).toSp(),
                    lineHeight = (size.width * 0.145f).toSp(),
                    letterSpacing = (-size.width * 0.006f).toSp(),
                ),
            )
            val editionLayout = measurer.measure(
                edition,
                TextStyle(
                    color = C.Cream,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = (size.width * 0.032f).toSp(),
                    letterSpacing = (size.width * 0.003f).toSp(),
                ),
            )
            onDrawBehind {
                drawRect(Brush.linearGradient(listOf(C.SleeveLight, C.Sleeve), Offset.Zero, Offset(size.width, size.height)))
                drawRect(C.SleeveDark.copy(alpha = 0.18f), style = Stroke(size.width * 0.009f))
                drawText(titleLayout, topLeft = Offset(size.width * 0.075f, size.height * 0.07f))
                val sun = Offset(size.width * 0.59f, size.height * 0.635f)
                drawCircle(C.Cream, size.width * 0.255f, sun)
                repeat(7) { index ->
                    val y = size.height * (0.58f + index * 0.042f)
                    drawRect(C.Sleeve, Offset(0f, y), Size(size.width, size.height * (0.008f + index * 0.003f)))
                }
                drawRect(C.SleeveDark, Offset(0f, size.height * 0.9f), Size(size.width, size.height * 0.1f))
                drawText(editionLayout, topLeft = Offset(size.width * 0.07f, size.height * 0.935f))
                repeat(220) { index ->
                    val point = Offset((index * 67 % 223) / 223f * size.width, (index * 113 % 227) / 227f * size.height)
                    drawCircle(C.SleeveDark.copy(alpha = 0.09f), size.width * 0.0013f, point)
                }
                drawLine(C.Cream.copy(alpha = 0.45f), Offset(2f, 0f), Offset(2f, size.height), 2f)
            }
        },
    )
}

@Composable
internal fun VinylTonearm(progress: () -> Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        normalized {
            val pivot = Offset(805f, 380f)
            drawRoundRect(C.DeckEdge, Offset(805f, 616f), Size(24f, 85f), CornerRadius(7f))
            drawCircle(C.Shadow.copy(alpha = 0.15f), 62f, pivot + Offset(4f, 8f))
            drawCircle(C.MetalDark, 55f, pivot)
            drawCircle(Brush.linearGradient(listOf(C.MetalLight, C.Metal, C.MetalDark)), 44f, pivot)
            rotate(progress() * 25f, pivot) {
                val arm = Path().apply {
                    moveTo(805f, 330f)
                    lineTo(805f, 661f)
                    quadraticTo(805f, 689f, 788f, 707f)
                    lineTo(766f, 731f)
                }
                translate(8f, 10f) {
                    drawPath(arm, C.Shadow.copy(alpha = 0.16f), style = Stroke(22f, cap = StrokeCap.Round))
                }
                drawPath(arm, C.MetalDark, style = Stroke(19f, cap = StrokeCap.Round))
                drawPath(
                    arm,
                    Brush.horizontalGradient(listOf(C.MetalDark, C.MetalLight, C.Metal), 755f, 818f),
                    style = Stroke(13f, cap = StrokeCap.Round),
                )
                drawLine(C.MetalLight, Offset(801f, 420f), Offset(801f, 657f), 3f, StrokeCap.Round)
                drawRoundRect(C.MetalDark, Offset(781f, 301f), Size(48f, 43f), CornerRadius(7f))
                drawLine(C.MetalLight.copy(alpha = 0.6f), Offset(786f, 312f), Offset(823f, 312f), 2f)
                rotate(42f, Offset(766f, 731f)) {
                    drawRoundRect(C.Record, Offset(747f, 707f), Size(37f, 65f), CornerRadius(5f))
                    drawRoundRect(C.MetalDark, Offset(753f, 710f), Size(24f, 33f), CornerRadius(3f))
                    drawRect(C.Accent, Offset(754f, 757f), Size(23f, 12f))
                }
            }
            drawCircle(C.MetalLight, 19f, pivot)
            drawCircle(C.MetalDark, 8f, pivot)
            drawLine(C.MetalLight, pivot - Offset(4f, 0f), pivot + Offset(4f, 0f), 1.5f)
        }
    }
}

@Composable
internal fun KineticVinylCover(modifier: Modifier = Modifier) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(modifier.clipToBounds()) {
            val width = maxWidth
            Canvas(Modifier.fillMaxSize()) {
                drawRect(C.Sleeve)
                drawCircle(C.SleeveLight, size.width * 0.48f, Offset(size.width * 0.25f, size.height * 0.75f))
                drawCircle(C.Cream.copy(alpha = 0.12f), size.width * 0.31f, Offset(size.width * 0.92f, 0f))
            }
            VinylRecord(
                angle = { -24f },
                modifier = Modifier
                    .offset(width * 0.40f, width * 0.035f)
                    .size(width * 0.64f)
                    .graphicsLayer {
                        shape = CircleShape
                        shadowElevation = 12.dp.toPx()
                    },
            )
            VinylSleeve(
                Modifier
                    .offset(width * 0.06f, width * 0.05f)
                    .size(width * 0.46f)
                    .graphicsLayer {
                        rotationZ = -12f
                        shadowElevation = 8.dp.toPx()
                    },
            )
        }
    }
}

private inline fun DrawScope.normalized(block: DrawScope.() -> Unit) {
    scale(size.width / 1_000f, size.height / 1_000f, Offset.Zero, block)
}
