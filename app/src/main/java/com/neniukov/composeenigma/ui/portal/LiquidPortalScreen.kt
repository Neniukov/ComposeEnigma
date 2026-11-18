package com.neniukov.composeenigma.ui.portal

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.theme.PortalColors as C

@Composable
internal fun LiquidPortalScreen(
    motion: PortalMotion,
    playing: Boolean,
    isActive: Boolean,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onTouch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sceneDescription = stringResource(R.string.portal_scene_description)
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(C.Background),
    ) {
        val compact = maxHeight < 720.dp
        val wide = maxWidth > maxHeight
        val chromeWidth = if (wide) maxWidth * 0.42f else 640.dp
        PortalCanvas(
            motion,
            Modifier
                .fillMaxSize()
                .semantics { contentDescription = sceneDescription }
                .pointerInput(motion, isActive, onTouch) {
                    if (!isActive) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        fun normalized(position: Offset) = Offset(
                            (position.x / size.width).coerceIn(0f, 1f),
                            (position.y / size.height).coerceIn(0f, 1f),
                        )
                        motion.press(normalized(down.position))
                        onTouch()
                        try {
                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (change.isConsumed) break
                                motion.target = normalized(change.position)
                                change.consume()
                            } while (event.changes.any { it.id == down.id && it.pressed })
                        } finally {
                            motion.touching = false
                        }
                    }
                },
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to C.Background.copy(alpha = 0.62f),
                        0.35f to Color.Transparent,
                        0.65f to Color.Transparent,
                        1f to C.Background.copy(alpha = 0.94f),
                    ),
                ),
        )
        Column(
            Modifier
                .align(if (wide) Alignment.TopStart else Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = chromeWidth)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp),
        ) {
            PortalHeader(onBack)
            Spacer(Modifier.height(if (compact) 20.dp else 38.dp))
            if (!wide) {
                Text(
                    stringResource(R.string.portal_eyebrow),
                    color = C.Accent,
                    fontSize = 9.sp,
                    letterSpacing = 2.5.sp,
                    lineHeight = 14.sp,
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(
                stringResource(R.string.portal_headline),
                color = C.White,
                fontFamily = FontFamily.Serif,
                fontSize = if (wide) {
                    40.sp
                } else if (compact) {
                    46.sp
                } else {
                    60.sp
                },
                lineHeight = if (wide) {
                    42.sp
                } else if (compact) {
                    47.sp
                } else {
                    59.sp
                },
                letterSpacing = (-2).sp,
            )
            if (!compact) {
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.portal_subtitle),
                    color = C.Muted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
            }
        }
        PortalFooter(
            playing,
            onTogglePlay,
            compact,
            wide,
            Modifier
                .align(if (wide) Alignment.BottomStart else Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = chromeWidth)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 24.dp),
        )
    }
}

@Composable
private fun PortalHeader(onBack: () -> Unit) {
    val backLabel = stringResource(R.string.portal_back)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(C.Glass)
                .border(1.dp, C.Line, CircleShape)
                .clickable(role = Role.Button, onClickLabel = backLabel, onClick = onBack)
                .semantics { contentDescription = backLabel },
            contentAlignment = Alignment.Center,
        ) {
            GlyphView(Glyph.Back, C.White, Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            stringResource(R.string.portal_brand),
            color = C.White,
            fontFamily = FontFamily.Serif,
            fontSize = 24.sp,
            letterSpacing = (-0.8).sp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PortalFooter(
    playing: Boolean,
    onTogglePlay: () -> Unit,
    compact: Boolean,
    wide: Boolean,
    modifier: Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (!wide) {
            Text(
                stringResource(R.string.portal_touch_hint),
                color = C.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(if (compact) 16.dp else 24.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(C.Line),
            )
            Spacer(Modifier.height(16.dp))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                WorldLabel(R.string.portal_world_night, C.Muted)
                WorldLabel(R.string.portal_world_desert, C.Accent)
            }
            Spacer(Modifier.width(12.dp))
            Row(
                Modifier
                    .heightIn(min = 48.dp)
                    .clip(CircleShape)
                    .background(C.Glass)
                    .border(1.dp, C.Line, CircleShape)
                    .clickable(role = Role.Button, onClick = onTogglePlay)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlyphView(if (playing) Glyph.Pause else Glyph.Play, C.White, Modifier.size(12.dp))
                Spacer(Modifier.width(9.dp))
                Text(
                    stringResource(if (playing) R.string.portal_pause else R.string.portal_play),
                    color = C.White,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun WorldLabel(@StringRes label: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(4.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.width(7.dp))
        Text(stringResource(label), color = color, fontSize = 8.sp, lineHeight = 12.sp, letterSpacing = 1.sp)
    }
}
