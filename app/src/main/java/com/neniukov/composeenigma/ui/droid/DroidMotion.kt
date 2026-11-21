package com.neniukov.composeenigma.ui.droid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

internal enum class DroidTrick { Wave, Flip }

/** Local visual state. Only the canvas observes the frame-rate values. */
@Stable
internal class DroidMotion {
    var playing by mutableStateOf(true)
    var time by mutableFloatStateOf(0f)
        private set
    var yaw by mutableFloatStateOf(-0.32f)
        private set
    var pitch by mutableFloatStateOf(0.08f)
        private set
    var trickProgress by mutableFloatStateOf(1f)
        private set
    var trick by mutableStateOf(DroidTrick.Wave)
        private set
    var dragging by mutableStateOf(false)
    private var targetYaw = yaw
    private var targetPitch = pitch
    private var yawVelocity = 0f
    private var pitchVelocity = 0f
    private var idleTime = 0f

    fun drag(dx: Float, dy: Float) {
        targetYaw += dx * 6f
        targetPitch = (targetPitch + dy * 3f).coerceIn(-0.55f, 0.65f)
    }

    fun perform(next: DroidTrick) {
        if (trickProgress < 1f) return
        trick = next
        trickProgress = 0f
        playing = true
    }

    fun step(deltaSeconds: Float) {
        val dt = deltaSeconds.coerceIn(0f, 0.032f)
        if (playing) {
            time += dt
            if (!dragging) idleTime += dt
            if (trickProgress < 1f) {
                trickProgress = (trickProgress + dt / if (trick == DroidTrick.Flip) 1.45f else 2.3f)
                    .coerceAtMost(1f)
            }
        }
        // A damped spring retains velocity when a gesture changes its target.
        val idleYaw = if (playing && !dragging) sin(idleTime * 0.65f) * 0.18f else 0f
        val damping = exp(-12f * dt)
        yawVelocity = (yawVelocity + ((targetYaw + idleYaw) - yaw) * 95f * dt) * damping
        pitchVelocity = (pitchVelocity + (targetPitch - pitch) * 95f * dt) * damping
        yaw += yawVelocity * dt
        pitch += pitchVelocity * dt
    }

    val isSettling: Boolean
        get() = abs(yawVelocity) + abs(pitchVelocity) + abs(targetYaw - yaw) + abs(targetPitch - pitch) > 0.0005f

    val flip: Float
        get() {
            if (trick != DroidTrick.Flip || trickProgress >= 1f) return 0f
            val p = ((trickProgress - 0.12f) / 0.76f).coerceIn(0f, 1f)
            return (p * p * (3f - 2f * p)) * (2f * PI.toFloat())
        }

    val lift: Float
        get() = if (trick == DroidTrick.Flip) sin(trickProgress * PI.toFloat()) * 0.62f else 0f

    val wave: Float
        get() = if (trick == DroidTrick.Wave) sin(trickProgress * PI.toFloat()).coerceAtLeast(0f) else 0f
}

@Composable
internal fun rememberDroidMotion(isActive: Boolean): DroidMotion {
    val motion = remember { DroidMotion() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(motion, lifecycle, isActive) {
        if (!isActive) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            androidx.compose.runtime.snapshotFlow { motion.playing to motion.dragging }
                .collectLatest {
                    var previous = 0L
                    while (motion.playing || motion.dragging || motion.isSettling) {
                        withFrameNanos { now ->
                            if (previous != 0L) motion.step((now - previous) / 1_000_000_000f)
                            previous = now
                        }
                    }
                }
        }
    }
    return motion
}
