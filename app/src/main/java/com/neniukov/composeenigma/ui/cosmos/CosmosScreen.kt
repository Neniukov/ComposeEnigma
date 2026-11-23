package com.neniukov.composeenigma.ui.cosmos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.neniukov.composeenigma.ui.theme.CosmosColors as C

@Composable
internal fun CosmosRoute(isActive: Boolean, onBack: () -> Unit) {
    val motion = rememberCosmosMotion(isActive)
    CosmosScreen(motion, isActive, onBack)
}

@Composable
internal fun CosmosScreen(motion: CosmosMotion, isActive: Boolean, onBack: () -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier.fillMaxSize().background(C.Background).windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val wide = maxWidth > maxHeight
        val compact = maxHeight < 740.dp
        val sceneHeight = (maxHeight - 445.dp).coerceIn(250.dp, 540.dp)
        val availableHeight = maxHeight
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = if (wide) 1100.dp else 600.dp).fillMaxWidth()) {
                Spacer(Modifier.height(18.dp))
                CosmosHeader(onBack, motion.playing, motion::togglePlaying)
                if (wide) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(0.42f)) {
                            CosmosIntroduction(compact = true)
                            Spacer(Modifier.height(24.dp))
                            CosmosControls(motion)
                        }
                        InteractiveCosmos(
                            motion,
                            isActive,
                            Modifier.weight(0.58f).height((availableHeight - 100.dp).coerceAtLeast(270.dp)),
                        )
                    }
                } else {
                    Spacer(Modifier.height(if (compact) 26.dp else 36.dp))
                    CosmosIntroduction(compact)
                    InteractiveCosmos(motion, isActive, Modifier.fillMaxWidth().height(sceneHeight))
                    CosmosControls(motion)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CosmosHeader(onBack: () -> Unit, playing: Boolean, onTogglePlay: () -> Unit) {
    val back = stringResource(R.string.cosmos_back)
    val playback = stringResource(if (playing) R.string.cosmos_pause else R.string.cosmos_play)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).border(1.dp, C.Line, CircleShape)
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = back },
            contentAlignment = Alignment.Center,
        ) {
            GlyphView(Glyph.Back, C.Ink, Modifier.size(19.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            stringResource(R.string.cosmos_brand),
            color = C.Ink,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-1).sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            stringResource(R.string.cosmos_number),
            color = C.Muted,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.width(16.dp))
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(C.Surface).border(1.dp, C.Line, CircleShape)
                .clickable(role = Role.Button, onClick = onTogglePlay)
                .semantics { contentDescription = playback },
            contentAlignment = Alignment.Center,
        ) {
            GlyphView(if (playing) Glyph.Pause else Glyph.Play, C.Cyan, Modifier.size(16.dp))
        }
    }
}

@Composable
private fun CosmosIntroduction(compact: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(5.dp).background(C.Cyan, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.cosmos_eyebrow),
            color = C.Cyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.8.sp,
        )
    }
    Spacer(Modifier.height(14.dp))
    Text(
        stringResource(R.string.cosmos_headline),
        color = C.Ink,
        fontSize = if (compact) 38.sp else 44.sp,
        lineHeight = if (compact) 42.sp else 48.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-1.8).sp,
    )
    Spacer(Modifier.height(12.dp))
    Text(
        stringResource(R.string.cosmos_subtitle),
        color = C.Muted,
        fontSize = 12.sp,
        lineHeight = 19.sp,
    )
}

@Composable
private fun InteractiveCosmos(motion: CosmosMotion, isActive: Boolean, modifier: Modifier) {
    val model = remember { CosmosModel() }
    val description = stringResource(R.string.cosmos_scene_description)
    val signal = stringResource(R.string.cosmos_signal)
    val left = stringResource(R.string.cosmos_rotate_left)
    val right = stringResource(R.string.cosmos_rotate_right)
    CosmosScene(
        motion,
        model,
        modifier
            .semantics {
                contentDescription = description
                customActions = listOf(
                    CustomAccessibilityAction(signal) {
                        motion.sendSignal()
                        true
                    },
                    CustomAccessibilityAction(left) {
                        motion.dragBy(-0.2f, 0f)
                        true
                    },
                    CustomAccessibilityAction(right) {
                        motion.dragBy(0.2f, 0f)
                        true
                    },
                )
            }
            .pointerInput(motion, isActive) {
                if (!isActive) return@pointerInput
                detectTapGestures { position ->
                    val viewport = CosmosViewport(size.width.toFloat(), size.height.toFloat())
                    val node = cosmosNearestNode(
                        model,
                        viewport,
                        position.x,
                        position.y,
                        motion.yaw + motion.phase,
                        motion.pitch,
                    )
                    motion.sendSignalFrom(node)
                }
            }
            .pointerInput(motion, isActive) {
                if (!isActive) return@pointerInput
                try {
                    detectDragGestures(
                        onDragStart = { motion.beginDrag() },
                        onDragEnd = motion::finishDrag,
                        onDragCancel = motion::finishDrag,
                    ) { change, amount ->
                        change.consume()
                        motion.dragBy(amount.x / size.width, amount.y / size.height)
                    }
                } finally {
                    motion.finishDrag()
                }
            },
    )
}

@Composable
private fun CosmosControls(motion: CosmosMotion) {
    val transmitting by remember(motion) { derivedStateOf { motion.signalProgress < 1f } }
    val status = when {
        !motion.playing -> R.string.cosmos_paused
        transmitting -> R.string.cosmos_transmitting
        else -> R.string.cosmos_ready
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.cosmos_edition),
                color = C.Muted,
                fontSize = 9.sp,
                lineHeight = 15.sp,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            Box(Modifier.size(4.dp).background(if (motion.playing) C.Cyan else C.Muted, CircleShape))
            Spacer(Modifier.width(7.dp))
            Text(
                stringResource(status),
                color = C.Cyan,
                fontSize = 8.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.7.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(C.Line))
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(18.dp)).background(C.Cyan)
                .clickable(role = Role.Button, onClick = motion::sendSignal)
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stringResource(R.string.cosmos_signal),
                color = C.Background,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            GlyphView(Glyph.Arrow, C.Background, Modifier.size(20.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.cosmos_hint),
            color = C.Muted,
            fontSize = 8.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.2.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Preview(name = "Neural Cosmos", widthDp = 393, heightDp = 852, apiLevel = 36)
@Preview(name = "Cosmos compact", widthDp = 360, heightDp = 640, apiLevel = 36)
@Preview(name = "Cosmos landscape", widthDp = 852, heightDp = 393, apiLevel = 36)
@Preview(name = "Cosmos large text", widthDp = 393, heightDp = 852, fontScale = 2f, apiLevel = 36)
@Composable
private fun CosmosPreview() {
    val motion = remember { CosmosMotion(initialPhase = 0.7f, initialSignal = 0.27f) }
    MotionStudiesTheme { CosmosScreen(motion, isActive = false, onBack = {}) }
}
