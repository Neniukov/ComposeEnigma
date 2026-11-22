package com.neniukov.composeenigma.ui.paper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme
import com.neniukov.composeenigma.ui.theme.PaperColors as C

@Composable
internal fun PaperRoute(isActive: Boolean, onBack: () -> Unit) {
    val motion = rememberPaperMotion(isActive)
    PaperScreen(motion, isActive, onBack)
}

@Composable
internal fun PaperScreen(motion: PaperMotion, isActive: Boolean, onBack: () -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize().paperStock().windowInsetsPadding(WindowInsets.safeDrawing)) {
        val sceneHeight = (maxHeight - 480.dp).coerceIn(230.dp, 440.dp)
        val availableHeight = maxHeight
        val compact = maxHeight < 740.dp
        val wide = maxWidth > maxHeight
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = if (wide) 1_000.dp else 560.dp).fillMaxWidth()) {
                Spacer(Modifier.height(18.dp))
                PaperHeader(onBack)
                if (wide) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(0.42f)) {
                            PaperIntroduction(compact = true, wide = true)
                            Spacer(Modifier.height(16.dp))
                            PaperControls(motion, compact = true)
                        }
                        PaperInteraction(
                            motion,
                            isActive,
                            Modifier.weight(0.58f)
                                .height((availableHeight - 100.dp).coerceAtLeast(230.dp)),
                        )
                    }
                } else {
                    Spacer(Modifier.height(if (compact) 28.dp else 38.dp))
                    PaperIntroduction(compact)
                    PaperInteraction(motion, isActive, Modifier.fillMaxWidth().height(sceneHeight))
                    PaperControls(motion)
                }
                Spacer(Modifier.height(26.dp))
            }
        }
    }
}

@Composable
private fun PaperIntroduction(compact: Boolean, wide: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(C.Clay, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.paper_eyebrow),
            color = C.Clay,
            fontSize = 9.sp,
            letterSpacing = if (wide) 1.sp else 1.8.sp,
            lineHeight = 14.sp,
        )
    }
    Spacer(Modifier.height(if (wide) 8.dp else 12.dp))
    Text(
        stringResource(R.string.paper_headline),
        color = C.Ink,
        fontFamily = FontFamily.Serif,
        fontSize = if (wide) {
            32.sp
        } else if (compact) {
            42.sp
        } else {
            48.sp
        },
        lineHeight = if (wide) {
            34.sp
        } else if (compact) {
            44.sp
        } else {
            50.sp
        },
        letterSpacing = (-2).sp,
    )
    Spacer(Modifier.height(if (wide) 8.dp else 12.dp))
    Text(stringResource(R.string.paper_subtitle), color = C.Muted, fontSize = 12.sp, lineHeight = 19.sp)
}

private fun Modifier.paperStock(): Modifier = drawWithCache {
    val flecks = List(650) { i -> Offset((i * 73 % 997) / 997f * size.width, (i * 193 % 991) / 991f * size.height) }
    onDrawBehind {
        drawRect(C.Background)
        flecks.forEach { drawCircle(C.Shadow.copy(alpha = 0.035f), 0.65f, it) }
    }
}

@Composable
private fun PaperHeader(onBack: () -> Unit) {
    val back = stringResource(R.string.paper_back)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).border(1.dp, C.Line, CircleShape)
                .clickable(role = Role.Button, onClick = onBack).semantics { contentDescription = back },
            Alignment.Center,
        ) {
            GlyphView(Glyph.Back, C.Ink, Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            stringResource(R.string.paper_brand),
            color = C.Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1.5).sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            stringResource(R.string.paper_number),
            color = C.Muted,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun PaperInteraction(motion: PaperMotion, isActive: Boolean, modifier: Modifier) {
    val description = stringResource(R.string.paper_scene_description)
    val open = stringResource(R.string.paper_open)
    val close = stringResource(R.string.paper_close)
    PaperScene(
        { motion.opening },
        modifier
            .semantics {
                contentDescription = description
                customActions = listOf(
                    CustomAccessibilityAction(open) {
                        motion.setOpen(true)
                        true
                    },
                    CustomAccessibilityAction(close) {
                        motion.setOpen(false)
                        true
                    },
                )
            }
            .pointerInput(motion, isActive) {
                if (!isActive) return@pointerInput
                try {
                    detectHorizontalDragGestures(
                        onDragStart = { motion.beginDrag() },
                        onDragEnd = motion::finishDrag,
                        onDragCancel = motion::finishDrag,
                    ) { change, amount ->
                        change.consume()
                        motion.dragBy(amount / size.width * 1.85f)
                    }
                } finally {
                    motion.finishDrag()
                }
            },
    )
}

@Composable
private fun PaperControls(motion: PaperMotion, compact: Boolean = false) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.paper_edition),
                color = C.Ink,
                fontSize = 10.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.paper_chapter),
                color = C.Muted,
                fontSize = 8.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
        Spacer(Modifier.height(if (compact) 4.dp else 12.dp))
        PaperProgress(motion)
        Spacer(Modifier.height(if (compact) 8.dp else 20.dp))
        val label = stringResource(if (motion.expanded) R.string.paper_close else R.string.paper_open)
        Row(
            Modifier.fillMaxWidth().heightIn(
                min = if (compact) 48.dp else 56.dp,
            ).clip(RoundedCornerShape(16.dp)).background(C.Ink)
                .clickable(role = Role.Button, onClick = { motion.setOpen(!motion.expanded) })
                .padding(horizontal = 22.dp, vertical = if (compact) 14.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, color = C.PageLight, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            GlyphView(if (motion.expanded) Glyph.Back else Glyph.Arrow, C.Gold, Modifier.size(20.dp))
        }
        Spacer(Modifier.height(if (compact) 8.dp else 16.dp))
        Text(
            stringResource(R.string.paper_hint),
            color = C.Muted,
            fontSize = 8.sp,
            letterSpacing = 1.4.sp,
            lineHeight = 14.sp,
        )
    }
}

@Composable
private fun PaperProgress(motion: PaperMotion) {
    val label = stringResource(R.string.paper_progress)
    Canvas(
        Modifier.fillMaxWidth().height(24.dp).semantics {
            contentDescription = label
            progressBarRangeInfo = ProgressBarRangeInfo(motion.opening, 0f..1f)
        },
    ) {
        val start = Offset(5.dp.toPx(), center.y)
        val end = Offset(size.width - 5.dp.toPx(), center.y)
        val marker = Offset(start.x + (end.x - start.x) * motion.opening, center.y)
        drawLine(C.Line, start, end, 1.dp.toPx(), StrokeCap.Round)
        drawLine(C.Clay, start, marker, 1.5.dp.toPx(), StrokeCap.Round)
        drawCircle(C.Background, 5.dp.toPx(), marker)
        drawCircle(C.Clay, 3.dp.toPx(), marker)
    }
}

@Preview(name = "Paper Worlds closed", widthDp = 393, heightDp = 852, apiLevel = 36)
@Preview(name = "Paper Worlds compact", widthDp = 360, heightDp = 640, apiLevel = 36)
@Preview(name = "Paper Worlds large text", widthDp = 393, heightDp = 852, fontScale = 2f, apiLevel = 36)
@Preview(name = "Paper Worlds landscape", widthDp = 852, heightDp = 393, apiLevel = 36)
@Composable
private fun PaperClosedPreview() {
    MotionStudiesTheme { PaperScreen(PaperMotion(), false, {}) }
}

@Preview(name = "Paper Worlds unfolded", widthDp = 393, heightDp = 852, apiLevel = 36)
@Composable
private fun PaperOpenPreview() {
    MotionStudiesTheme { PaperScreen(PaperMotion(1f, true), false, {}) }
}
