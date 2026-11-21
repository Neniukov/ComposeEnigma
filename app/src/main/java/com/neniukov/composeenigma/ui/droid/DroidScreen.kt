package com.neniukov.composeenigma.ui.droid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
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
import com.neniukov.composeenigma.ui.theme.DroidColors as C

@Composable
internal fun DroidRoute(isActive: Boolean, onBack: () -> Unit) {
    val motion = rememberDroidMotion(isActive)
    DroidScreen(motion, isActive, onBack)
}

@Composable
internal fun DroidScreen(motion: DroidMotion, isActive: Boolean, onBack: () -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize().background(C.Background).windowInsetsPadding(WindowInsets.safeDrawing)) {
        val wide = maxWidth > maxHeight
        val compact = maxHeight < 740.dp
        val availableHeight = maxHeight
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(20.dp))
            DroidHeader(onBack, motion.playing, onTogglePlay = { motion.playing = !motion.playing })
            if (wide) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(0.44f).padding(top = 28.dp, bottom = 24.dp)) {
                        DroidIntroduction(compact = true)
                        Spacer(Modifier.height(28.dp))
                        DroidControls(motion)
                    }
                    InteractiveDroid(motion, isActive, Modifier.weight(0.56f).height((availableHeight - 90.dp).coerceAtLeast(260.dp)))
                }
            } else {
                Spacer(Modifier.height(if (compact) 24.dp else 36.dp))
                DroidIntroduction(compact)
                InteractiveDroid(
                    motion, isActive,
                    Modifier.fillMaxWidth().height((availableHeight - if (compact) 410.dp else 450.dp).coerceIn(210.dp, 560.dp)),
                )
                DroidControls(motion)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DroidHeader(onBack: () -> Unit, playing: Boolean, onTogglePlay: () -> Unit) {
    val back = stringResource(R.string.droid_back)
    val play = stringResource(if (playing) R.string.droid_pause else R.string.droid_play)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(CircleShape).border(1.dp, C.Line, CircleShape)
            .clickable(role = Role.Button, onClick = onBack).semantics { contentDescription = back }, Alignment.Center) {
            GlyphView(Glyph.Back, C.Ink, Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(stringResource(R.string.droid_brand), color = C.Ink, fontSize = 23.sp, fontWeight = FontWeight.Bold,
            letterSpacing = (-1.2).sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(48.dp).clip(CircleShape).background(C.White).border(1.dp, C.Line, CircleShape)
            .clickable(role = Role.Button, onClick = onTogglePlay).semantics { contentDescription = play }, Alignment.Center) {
            GlyphView(if (playing) Glyph.Pause else Glyph.Play, C.Ink, Modifier.size(17.dp))
        }
    }
}

@Composable
private fun DroidIntroduction(compact: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(C.DarkGreen, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.droid_eyebrow), color = C.DarkGreen, fontSize = 9.sp, letterSpacing = 2.sp, lineHeight = 14.sp)
    }
    Spacer(Modifier.height(12.dp))
    Text(stringResource(R.string.droid_headline), color = C.Ink,
        fontFamily = FontFamily.Serif, fontSize = if (compact) 48.sp else 62.sp,
        lineHeight = if (compact) 49.sp else 62.sp, letterSpacing = (-2.5).sp)
    Spacer(Modifier.height(10.dp))
    Text(stringResource(R.string.droid_subtitle), color = C.Muted, fontSize = 12.sp, lineHeight = 19.sp)
}

@Composable
private fun InteractiveDroid(motion: DroidMotion, isActive: Boolean, modifier: Modifier) {
    val description = stringResource(R.string.droid_scene_description)
    val left = stringResource(R.string.droid_rotate_left)
    val right = stringResource(R.string.droid_rotate_right)
    DroidScene(motion, modifier
        .semantics {
            contentDescription = description
            customActions = listOf(
                CustomAccessibilityAction(left) { motion.drag(-0.2f, 0f); motion.playing = true; true },
                CustomAccessibilityAction(right) { motion.drag(0.2f, 0f); motion.playing = true; true },
            )
        }
        .pointerInput(motion, isActive) {
            if (!isActive) return@pointerInput
            try {
                detectDragGestures(
                    onDragStart = { motion.dragging = true },
                    onDragEnd = { motion.dragging = false },
                    onDragCancel = { motion.dragging = false },
                ) { change, amount ->
                    change.consume()
                    motion.drag(amount.x / size.width, amount.y / size.height)
                }
            } finally {
                motion.dragging = false
            }
        })
}

@Composable
private fun DroidControls(motion: DroidMotion) {
    val ready by remember(motion) { derivedStateOf { motion.trickProgress >= 1f } }
    Column(Modifier.widthIn(max = 520.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.droid_hint), color = C.Muted, fontSize = 9.sp, letterSpacing = 1.5.sp, lineHeight = 15.sp)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DroidButton(stringResource(R.string.droid_wave), primary = true, enabled = ready, modifier = Modifier.weight(1f)) { motion.perform(DroidTrick.Wave) }
            DroidButton(stringResource(R.string.droid_flip), primary = false, enabled = ready, modifier = Modifier.weight(1f)) { motion.perform(DroidTrick.Flip) }
        }
        Spacer(Modifier.height(22.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(C.Line))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.droid_footer), color = C.Muted, fontSize = 8.sp, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.droid_number), color = C.DarkGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun DroidButton(label: String, primary: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.heightIn(min = 54.dp).clip(CircleShape)
        .background(if (primary) C.Ink else C.White).border(1.dp, if (primary) C.Ink else C.Line, CircleShape)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 16.dp), Alignment.Center) {
        Text(label, color = (if (primary) C.White else C.Ink).copy(alpha = if (enabled) 1f else 0.45f), fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 19.sp)
    }
}

@Preview(name = "Hello Android", widthDp = 393, heightDp = 852, apiLevel = 36)
@Preview(name = "Android compact", widthDp = 360, heightDp = 640, apiLevel = 36)
@Preview(name = "Android landscape", widthDp = 852, heightDp = 393, apiLevel = 36)
@Preview(name = "Android large text", widthDp = 393, heightDp = 852, fontScale = 2f, apiLevel = 36)
@Composable
private fun DroidPreview() {
    MotionStudiesTheme { DroidScreen(DroidMotion(), isActive = false, onBack = {}) }
}
