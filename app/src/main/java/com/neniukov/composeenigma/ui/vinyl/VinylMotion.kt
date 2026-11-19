package com.neniukov.composeenigma.ui.vinyl

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.neniukov.composeenigma.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal enum class VinylSpeed(@param:StringRes val label: Int, val degreesPerSecond: Float) {
    LongPlay(R.string.vinyl_speed_33, 200f),
    Single(R.string.vinyl_speed_45, 270f),
}

@Stable
internal class VinylMotion(initialPlacement: Float = 0f, initialArm: Float = 0f) {
    val placement = Animatable(initialPlacement)
    val arm = Animatable(initialArm)

    var loaded by mutableStateOf(true)
        private set
    var playing by mutableStateOf(true)
        private set
    var speed by mutableStateOf(VinylSpeed.LongPlay)
        private set
    var angle by mutableFloatStateOf(0f)
        private set
    var velocity by mutableFloatStateOf(0f)
        private set
    var touching by mutableStateOf(false)
        private set

    val canTouch: Boolean
        get() = loaded && placement.value == 1f && !placement.isRunning

    val motorVelocity: Float
        get() = if (canTouch && playing && arm.value > 0.9f && !touching) speed.degreesPerSecond else 0f

    fun togglePlayback() {
        if (!loaded) loaded = true
        playing = !playing
    }

    fun eject() {
        loaded = false
        playing = false
        stopTouch()
    }

    fun selectSpeed(speed: VinylSpeed) {
        this.speed = speed
    }

    fun grab(): Boolean {
        if (!canTouch) return false
        touching = true
        velocity = 0f
        return true
    }

    fun dragBy(degrees: Float) {
        if (touching && degrees.isFinite()) angle = normalizeDegrees(angle + degrees)
    }

    fun release(degreesPerSecond: Float) {
        if (!touching) return
        touching = false
        velocity = if (degreesPerSecond.isFinite()) degreesPerSecond.coerceIn(-1_080f, 1_080f) else 0f
    }

    fun spin(degreesPerSecond: Float): Boolean {
        if (!grab()) return false
        release(degreesPerSecond)
        return true
    }

    fun stopTouch() {
        touching = false
        velocity = 0f
    }

    fun advance(elapsedSeconds: Float) {
        if (touching) return
        val step = advanceRotor(angle, velocity, motorVelocity, elapsedSeconds)
        angle = step.angle
        velocity = step.velocity
    }

    companion object {
        val Saver = mapSaver<VinylMotion>(
            save = { motion ->
                mapOf(
                    "loaded" to motion.loaded,
                    "playing" to motion.playing,
                    "speed" to motion.speed.name,
                    "angle" to motion.angle,
                    "placement" to motion.placement.value,
                    "arm" to motion.arm.value,
                )
            },
            restore = { saved ->
                VinylMotion(
                    initialPlacement = saved.getValue("placement") as Float,
                    initialArm = saved.getValue("arm") as Float,
                ).apply {
                    loaded = saved.getValue("loaded") as Boolean
                    playing = saved.getValue("playing") as Boolean
                    speed = VinylSpeed.valueOf(saved.getValue("speed") as String)
                    angle = saved.getValue("angle") as Float
                }
            },
        )
    }
}

@Composable
internal fun rememberVinylMotion(isActive: Boolean): VinylMotion {
    val motion = rememberSaveable(saver = VinylMotion.Saver) { VinylMotion() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(motion, lifecycle, isActive) {
        if (!isActive) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            animateVinyl(motion)
        }
    }
    return motion
}

internal suspend fun animateVinyl(motion: VinylMotion) {
    try {
        coroutineScope {
            launch {
                snapshotFlow { motion.loaded }.collectLatest { loaded ->
                    val target = if (loaded) 1f else 0f
                    if (motion.placement.value == target) return@collectLatest
                    if (!loaded) {
                        snapshotFlow { motion.arm.value == 0f && !motion.arm.isRunning }.first { it }
                    }
                    motion.placement.animateTo(
                        target,
                        tween(if (loaded) 1_650 else 1_400, easing = FastOutSlowInEasing),
                    )
                }
            }
            launch {
                snapshotFlow { motion.loaded && motion.playing && motion.canTouch }.collect { playing ->
                    val target = if (playing) 1f else 0f
                    if (motion.arm.value == target && !motion.arm.isRunning) return@collect
                    launch {
                        motion.arm.animateTo(
                            target,
                            spring(dampingRatio = 1f, stiffness = 85f),
                        )
                    }
                }
            }
            launch {
                snapshotFlow { !motion.touching && (motion.motorVelocity != 0f || motion.velocity != 0f) }
                    .collectLatest { moving ->
                        if (moving) {
                            var previous = withFrameNanos { it }
                            while (true) {
                                val now = withFrameNanos { it }
                                motion.advance(((now - previous) / 1_000_000_000f).coerceIn(0f, 0.05f))
                                previous = now
                            }
                        }
                    }
            }
        }
    } finally {
        motion.stopTouch()
    }
}
