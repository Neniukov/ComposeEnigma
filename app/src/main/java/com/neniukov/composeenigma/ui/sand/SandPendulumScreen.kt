package com.neniukov.composeenigma.ui.sand

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme
import com.neniukov.composeenigma.ui.theme.SandColors as C

private class SandTrayViewModel : ViewModel() {
    val motion = SandMotion()
}

@Composable
internal fun SandPendulumRoute(isActive: Boolean, onBack: () -> Unit) {
    val tray = viewModel { SandTrayViewModel() }
    val motion = tray.motion
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(motion, lifecycle, isActive) {
        if (isActive) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { motion.animate() }
        }
    }
    SandPendulumScreen(motion, isActive, onBack)
}

@Composable
private fun SandPendulumScreen(motion: SandMotion, isActive: Boolean, onBack: () -> Unit) {
    val background = remember { Brush.radialGradient(listOf(C.Glow, C.Background), radius = 880f) }
    BoxWithConstraints(
        Modifier.fillMaxSize().background(background).windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter,
    ) {
        val landscape = maxWidth > maxHeight && maxWidth >= 600.dp
        if (landscape) {
            val trayWidth = minOf((maxHeight - 38.dp).coerceAtLeast(220.dp), maxWidth * 0.5f, 560.dp)
            Row(
                Modifier.widthIn(max = 1060.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 30.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(25.dp)) {
                    SandNavigation(isActive, onBack)
                    SandIntroduction(centered = false)
                    SandControls(motion, isActive)
                }
                SandStage(motion, isActive, Modifier.width(trayWidth))
            }
        } else {
            val extraHeight = (maxHeight - minOf(maxWidth, 570.dp) - 235.dp).coerceIn(0.dp, 210.dp)
            Column(
                Modifier.widthIn(max = 570.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SandNavigation(isActive, onBack)
                SandIntroduction(centered = true, modifier = Modifier.padding(top = extraHeight / 3f))
                SandStage(motion, isActive, Modifier.fillMaxWidth())
                SandControls(motion, isActive)
            }
        }
    }
}

@Composable
private fun SandNavigation(isActive: Boolean, onBack: () -> Unit) {
    val description = stringResource(R.string.sand_back)
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier.size(48.dp)
                .clip(CircleShape)
                .border(1.dp, C.Border, CircleShape)
                .clickable(enabled = isActive, role = Role.Button, onClick = onBack)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            GlyphView(Glyph.Back, C.Accent, Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SandIntroduction(centered: Boolean, modifier: Modifier = Modifier) {
    val alignment = if (centered) TextAlign.Center else TextAlign.Start
    Column(
        modifier,
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.sand_title),
            Modifier.semantics { heading() },
            color = C.Foreground,
            fontFamily = FontFamily.Serif,
            fontSize = 44.sp,
            lineHeight = 47.sp,
            letterSpacing = (-1.5).sp,
            textAlign = alignment,
        )
        Text(
            stringResource(R.string.sand_hint),
            color = C.Muted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = alignment,
        )
    }
}

@Composable
private fun SandControls(motion: SandMotion, isActive: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(
            onClick = motion::setSwinging,
            enabled = isActive && motion.canHandleTrayInput,
            colors = ButtonDefaults.buttonColors(containerColor = C.Accent, contentColor = C.ButtonInk),
        ) {
            Text(stringResource(R.string.sand_swing))
        }
        TextButton(
            onClick = motion::rake,
            enabled = isActive && motion.canHandleTrayInput,
            colors = ButtonDefaults.textButtonColors(contentColor = C.Foreground),
        ) {
            Text(stringResource(R.string.sand_rake))
        }
    }
}

@Composable
private fun SandStage(motion: SandMotion, isActive: Boolean, modifier: Modifier) {
    val description = stringResource(R.string.sand_stage_description)
    SandDrawing(
        motion,
        modifier.aspectRatio(1f)
            .semantics { contentDescription = description }
            .sandInput(motion, isActive),
    )
}

private fun Modifier.sandInput(motion: SandMotion, isActive: Boolean): Modifier = pointerInput(motion, isActive) {
    if (!isActive) return@pointerInput
    awaitEachGesture {
        val down = awaitFirstDown()
        if (size.width == 0) return@awaitEachGesture
        val scale = SAND_STAGE.toFloat() / size.width
        val start = (down.position * scale).toSandPosition()
        val hitRadius = (24.dp.toPx() * scale).toSandDistance()
        val drag = motion.tryGrab(start, hitRadius, down.uptimeMillis) ?: return@awaitEachGesture

        down.consume()
        try {
            while (true) {
                val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                if (change.isConsumed) break
                change.consume()
                change.historical.forEach { sample ->
                    drag.move((sample.position * scale).toSandPosition(), sample.uptimeMillis)
                }
                val pointer = (change.position * scale).toSandPosition()
                if (!change.pressed) {
                    val velocity = drag.finish(pointer, change.uptimeMillis)
                    motion.pull(drag.position)
                    motion.release(velocity)
                    break
                }
                motion.pull(drag.move(pointer, change.uptimeMillis))
            }
        } finally {
            motion.cancelGrab()
        }
    }
}

@Composable
private fun SandDrawing(motion: SandMotion, modifier: Modifier) {
    val artwork = remember { SandArtwork() }
    val lift = animateFloatAsState(
        targetValue = if (motion.held) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "Stylus lift",
    )
    Canvas(modifier.clipToBounds()) {
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            val saved = native.save()
            native.scale(size.width / SAND_STAGE, size.height / SAND_STAGE)
            motion.draw(native, artwork, lift.value)
            native.restoreToCount(saved)
        }
    }
}

@Composable
internal fun SandPendulumCover(modifier: Modifier = Modifier) {
    Image(
        bitmap = SandArtwork.cover,
        contentDescription = null,
        modifier = modifier.background(C.Background),
    )
}

@Preview(name = "Sand Pendulum", widthDp = 393, heightDp = 852, apiLevel = 36)
@Preview(name = "Sand Pendulum landscape", widthDp = 852, heightDp = 393, apiLevel = 36)
@Composable
private fun SandPendulumPreview() {
    val motion = remember { SandMotion() }
    MotionStudiesTheme { SandPendulumScreen(motion, isActive = true, onBack = {}) }
}
