package com.neniukov.composeenigma.ui.journey

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.theme.JourneyColors as C

private val MinimumHomeHeight = 852.dp
private val MinimumHeroHeight = 280.dp

@Composable
internal fun JourneyScreen(
    progress: State<Float>,
    expanded: Boolean,
    saved: Boolean,
    playing: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
    transitionRunning: Boolean = false,
) {
    val homeScroll = rememberScrollState()
    val homeEnabled = !expanded && !transitionRunning
    val detailsEnabled = expanded && !transitionRunning
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(C.Paper)
    ) {
        val density = LocalDensity.current
        val w = with(density) { maxWidth.toPx() }
        val h = with(density) { maxHeight.toPx() }
        val homeHeight = maxOf(maxHeight, MinimumHomeHeight * density.fontScale.coerceAtLeast(1f))
        val homeHeightPx = with(density) { homeHeight.toPx() }
        val margin = with(density) { 24.dp.toPx() }
        val cardTop = homeHeightPx * .30f
        val cardHeight = homeHeightPx * .475f
        val heroHeight = with(density) {
            maxOf(maxHeight * .555f, MinimumHeroHeight * fontScale.coerceAtLeast(1f))
                .coerceAtMost(maxHeight * .7f).toPx()
        }
        val sheetStart = h + with(density) { 48.dp.toPx() }
        val start = Rect(margin, cardTop, w - margin, cardTop + cardHeight)
        val end = Rect(0f, 0f, w, heroHeight)

        Box(
            Modifier
                .fillMaxSize()
                .verticalScroll(homeScroll, enabled = !expanded && !transitionRunning)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(homeHeight)
            ) {
                HomeChrome(progress, homeEnabled, onPlay, playing, with(density) { cardTop.toDp() })
                Hero(
                    progress, homeEnabled, saved, onOpen, onSave,
                    Modifier.morphBounds(progress, start, end, endOffsetY = { homeScroll.value }),
                )
            }
        }
        DetailSheet(
            progress, detailsEnabled, saved, onSave,
            Modifier.morphBounds(
                progress,
                Rect(0f, sheetStart, w, sheetStart + h - heroHeight + margin),
                Rect(0f, heroHeight - margin, w, h),
            ),
        )
        CircleControl(
            kind = Glyph.Back,
            label = stringResource(R.string.close_escape),
            onClick = onClose,
            modifier = Modifier
                .padding(start = 24.dp, top = 44.dp)
                .reveal(progress, JourneyMotion.BackReveal),
            enabled = expanded,
        )
        if (playing && expanded) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    stringResource(R.string.stop_journey), color = C.Muted, fontSize = 10.sp,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable(role = Role.Button, onClick = onPlay)
                        .padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeChrome(
    progress: State<Float>,
    enabled: Boolean,
    onPlay: () -> Unit,
    playing: Boolean,
    cardTop: Dp,
) {
    val collectionLabelOffset = 27.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
    val semantics = if (enabled) Modifier else Modifier.clearAndSetSemantics {}
    Box(
        Modifier
            .fillMaxSize()
            .then(semantics)
            .graphicsLayer {
                alpha = 1f - JourneyMotion.HomeExit.valueAt(progress.value)
                translationY = progress.value * JourneyMotion.HomeExit.offsetY.toPx()
            }) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 36.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.brand), fontFamily = FontFamily.Serif, fontSize = 35.sp,
                    fontWeight = FontWeight.Bold, color = C.Ink, letterSpacing = (-2).sp
                )
                Spacer(Modifier.weight(1f))
                GlyphView(Glyph.Compass, C.Ink, Modifier.size(36.dp))
            }
            Spacer(Modifier.height(22.dp))
            Eyebrow(R.string.edition, color = C.Muted, size = 8)
            Spacer(Modifier.height(9.dp))
            Text(
                stringResource(R.string.headline), fontFamily = FontFamily.Serif, fontSize = 45.sp,
                lineHeight = 46.sp, letterSpacing = (-1.8).sp, color = C.Ink
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 27.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(R.string.issue, color = C.Muted, size = 8)
                Spacer(Modifier.weight(1f))
                repeat(3) { index ->
                    Box(
                        Modifier
                            .padding(start = 5.dp)
                            .width(if (index == 0) 18.dp else 5.dp)
                            .height(5.dp)
                            .background(if (index == 0) C.Accent else C.Line, CircleShape)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(CircleShape)
                    .border(1.dp, C.Line, CircleShape)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onPlay)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlyphView(if (playing) Glyph.Pause else Glyph.Play, C.Accent, Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(if (playing) R.string.stop_journey else R.string.play_journey),
                    color = C.Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(R.string.index),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = C.Muted
                )
            }
        }
        Row(
            Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = cardTop - collectionLabelOffset),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Eyebrow(R.string.curated_escapes, color = C.Muted, size = 9)
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.index),
                fontSize = 10.sp,
                color = C.Accent,
                fontFamily = FontFamily.Monospace
            )
            Text(
                stringResource(R.string.collection_count),
                fontSize = 10.sp,
                color = C.Muted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
