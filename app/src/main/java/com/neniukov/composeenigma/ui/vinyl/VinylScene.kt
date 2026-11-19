package com.neniukov.composeenigma.ui.vinyl

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import com.neniukov.composeenigma.R
import kotlin.math.PI
import kotlin.math.sin

@Composable
internal fun VinylScene(motion: VinylMotion, interactive: Boolean, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.vinyl_scene_description)
    val clockwise = stringResource(R.string.vinyl_spin_clockwise)
    val counterclockwise = stringResource(R.string.vinyl_spin_counterclockwise)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier
                .aspectRatio(1f)
                .semantics {
                    contentDescription = description
                    if (interactive) {
                        customActions = listOf(
                            CustomAccessibilityAction(clockwise) { motion.spin(650f) },
                            CustomAccessibilityAction(counterclockwise) { motion.spin(-650f) },
                        )
                    }
                },
        ) {
            val stage = maxWidth
            VinylDeck(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = (motion.placement.value * 1.8f).coerceIn(0f, 1f)
                        scaleX = 0.97f + motion.placement.value * 0.03f
                        scaleY = scaleX
                    },
            )
            VinylRecord(
                angle = { motion.angle },
                modifier = Modifier
                    .offset(stage * 0.11f, stage * 0.275f)
                    .size(stage * 0.6f)
                    .graphicsLayer {
                        val progress = motion.placement.value
                        val arc = sin(progress * PI).toFloat()
                        val stagePixels = size.width / 0.6f
                        translationX = stagePixels * (0.16f * (1f - progress) + arc * 0.18f)
                        translationY = stagePixels * (-0.125f * (1f - progress) - arc * 0.18f)
                        scaleX = 0.9f + progress * 0.1f + arc * 0.05f
                        scaleY = scaleX
                        rotationZ = -22f * (1f - progress)
                        shape = CircleShape
                        shadowElevation = (7f + arc * 18f) * density
                    }
                    .vinylInput(motion, interactive),
            )
            VinylSleeve(
                Modifier
                    .offset(stage * 0.055f, stage * 0.155f)
                    .size(stage * 0.59f)
                    .graphicsLayer {
                        val progress = motion.placement.value
                        val stagePixels = size.width / 0.59f
                        translationX = stagePixels * -0.13f * progress
                        translationY = stagePixels * -0.275f * progress
                        scaleX = 1f - progress * 0.46f
                        scaleY = scaleX
                        rotationZ = -9f - progress * 7f
                        shadowElevation = 8f * density
                    },
            )
            VinylTonearm(
                progress = { motion.arm.value },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = (motion.placement.value * 2f - 1f).coerceIn(0f, 1f) },
            )
        }
    }
}

private fun Modifier.vinylInput(motion: VinylMotion, enabled: Boolean): Modifier = pointerInput(motion, enabled) {
    if (!enabled) return@pointerInput
    awaitEachGesture {
        val down = awaitFirstDown()
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f
        val initial = down.position - center
        if (initial.getDistance() !in radius * 0.18f..radius || !motion.grab()) return@awaitEachGesture
        down.consume()
        val tracker = VelocityTracker()
        var previous: Float? = pointerAngle(initial.x, initial.y)
        var travel = 0f
        var releasedVelocity = 0f
        tracker.addPosition(down.uptimeMillis, Offset.Zero)
        try {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (change.isConsumed) break
                val position = change.position - center
                if (position.getDistance() < radius * 0.18f) {
                    previous = null
                    tracker.resetTracking()
                } else {
                    val current = pointerAngle(position.x, position.y)
                    previous?.let { angle ->
                        val delta = angularDelta(angle, current)
                        travel += delta
                        motion.dragBy(delta)
                    }
                    previous = current
                    tracker.addPosition(change.uptimeMillis, Offset(travel, 0f))
                }
                change.consume()
                if (!change.pressed) {
                    releasedVelocity = tracker.calculateVelocity().x
                    break
                }
            }
        } finally {
            motion.release(releasedVelocity)
        }
    }
}
