package com.neniukov.composeenigma.ui.journey

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.theme.JourneyColors as C

@Composable
internal fun DetailSheet(
    progress: State<Float>,
    enabled: Boolean,
    saved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier,
) {
    val semantics = if (enabled) Modifier else Modifier.clearAndSetSemantics {}
    val scroll = rememberScrollState()
    BoxWithConstraints(
        modifier
            .then(semantics)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(C.Paper)
    ) {
        Column(
            Modifier
                .verticalScroll(scroll, enabled = enabled)
                .heightIn(min = maxHeight)
                .padding(horizontal = 26.dp)
                .padding(top = 24.dp, bottom = 38.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.reveal(progress, JourneyMotion.IntroductionReveal)) {
                Eyebrow(R.string.detail_eyebrow, C.Accent, 8)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.detail_headline), fontFamily = FontFamily.Serif,
                    fontSize = 33.sp, lineHeight = 35.sp, letterSpacing = (-.8).sp, color = C.Ink
                )
                Spacer(Modifier.height(9.dp))
                Text(
                    stringResource(R.string.detail_copy),
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    color = C.Muted
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .reveal(progress, JourneyMotion.StatsReveal),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Stat(R.string.altitude, R.string.altitude_label, Modifier.weight(1f))
                Box(
                    Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(C.Line)
                )
                Stat(R.string.distance, R.string.distance_label, Modifier.weight(1f))
                Box(
                    Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(C.Line)
                )
                Stat(R.string.duration, R.string.duration_label, Modifier.weight(1f))
            }
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .reveal(progress, JourneyMotion.TrailReveal)
                        .background(C.Sage, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrailMark(Modifier.size(42.dp, 38.dp), progress)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.trail_name),
                            color = C.Ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(stringResource(R.string.trail_caption), color = C.Muted, fontSize = 9.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .reveal(progress, JourneyMotion.SaveReveal)
                        .clip(CircleShape)
                        .background(C.Accent, CircleShape)
                        .clickable(enabled = enabled, role = Role.Button, onClick = onSave)
                        .padding(horizontal = 21.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(if (saved) R.string.saved_escape else R.string.save_escape),
                        color = C.White, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    GlyphView(if (saved) Glyph.Check else Glyph.Arrow, C.White, Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun Stat(@StringRes value: Int, @StringRes label: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(stringResource(value), fontSize = 20.sp, fontFamily = FontFamily.Serif, color = C.Ink)
        Spacer(Modifier.height(4.dp))
        Eyebrow(label, C.Muted, 7)
    }
}

@Composable
private fun TrailMark(modifier: Modifier, progress: State<Float>) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * .1f, size.height * .85f)
            cubicTo(
                size.width * .95f,
                size.height * .8f,
                -size.width * .15f,
                size.height * .27f,
                size.width * .88f,
                size.height * .12f
            )
        }
        drawPath(
            path, C.Accent, style = Stroke(
                1.5.dp.toPx(), cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(5.dp.toPx(), 3.dp.toPx()),
                    (1f - progress.value) * JourneyMotion.TrailDashTravel.toPx()
                )
            )
        )
        drawCircle(C.Accent, 3.dp.toPx(), Offset(size.width * .1f, size.height * .85f))
        drawCircle(C.Accent, 3.dp.toPx(), Offset(size.width * .88f, size.height * .12f))
    }
}
