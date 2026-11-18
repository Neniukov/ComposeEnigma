package com.neniukov.composeenigma.ui.portal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val PositionVisibilityThreshold = Offset(0.001f, 0.001f)

@Stable
internal class PortalMotion(
    initialOpening: Float = 0f,
    initialCenter: Offset = Home,
    initialRipple: Float = 1f,
) {
    val center: Animatable<Offset, AnimationVector2D> = Animatable(
        initialCenter,
        Offset.VectorConverter,
        visibilityThreshold = PositionVisibilityThreshold,
    )
    val opening = Animatable(initialOpening)
    val ripple = Animatable(initialRipple)
    var target by mutableStateOf(initialCenter)
    var touching by mutableStateOf(false)
    var autoplayOpen by mutableStateOf(false)
    var time by mutableFloatStateOf(0f)
    var rippleOrigin by mutableStateOf(initialCenter)

    fun press(position: Offset) {
        target = position
        touching = true
    }

    companion object {
        val Home = Offset(0.5f, 0.55f)
    }
}

@Composable
internal fun rememberPortalMotion(isActive: Boolean, playing: Boolean, home: Offset): PortalMotion {
    val motion = remember { PortalMotion(initialCenter = home) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(home) {
        motion.touching = false
        motion.target = home
    }

    LaunchedEffect(motion, lifecycle, isActive) {
        if (!isActive) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            animatePortal(motion)
        }
    }

    LaunchedEffect(playing, isActive, lifecycle, home) {
        if (!playing || !isActive) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            try {
                while (true) {
                    motion.target = home
                    motion.autoplayOpen = true
                    delay(1_900.milliseconds)
                    motion.target = home + Offset(if (home.x > 0.6f) 0.08f else 0.14f, -0.07f)
                    delay(1_500.milliseconds)
                    motion.target = home + Offset(if (home.x > 0.6f) -0.08f else -0.14f, 0.04f)
                    delay(1_700.milliseconds)
                    motion.target = home
                    delay(1_200.milliseconds)
                    motion.autoplayOpen = false
                    delay(2_100.milliseconds)
                }
            } finally {
                motion.autoplayOpen = false
            }
        }
    }
    return motion
}

internal suspend fun animatePortal(motion: PortalMotion) {
    motion.touching = false
    motion.opening.snapTo(0f)
    motion.ripple.snapTo(1f)
    coroutineScope {
        var rippleAnimation: Job? = null
        launch {
            snapshotFlow { motion.target }.collect { target ->
                launch {
                    motion.center.animateTo(
                        target,
                        spring(
                            dampingRatio = 0.68f,
                            stiffness = 115f,
                            visibilityThreshold = PositionVisibilityThreshold,
                        ),
                    )
                }
            }
        }
        launch {
            snapshotFlow { motion.touching || motion.autoplayOpen }.collect { open ->
                if (!open && motion.opening.value > 0.05f) {
                    motion.rippleOrigin = motion.center.value
                    rippleAnimation?.cancel()
                    rippleAnimation = this@coroutineScope.launch {
                        motion.ripple.snapTo(0f)
                        motion.ripple.animateTo(1f, tween(1_450))
                    }
                }
                launch {
                    motion.opening.animateTo(
                        if (open) 1f else 0f,
                        spring(dampingRatio = if (open) 0.62f else 0.8f, stiffness = if (open) 95f else 70f),
                    )
                }
            }
        }
        launch {
            snapshotFlow {
                motion.touching || motion.autoplayOpen || motion.opening.isRunning || motion.ripple.isRunning
            }.collectLatest { moving ->
                if (moving) {
                    var previous = 0L
                    while (true) {
                        withFrameNanos { now ->
                            if (previous != 0L) {
                                motion.time += ((now - previous) / 1_000_000_000f).coerceAtMost(0.05f)
                            }
                            previous = now
                        }
                    }
                }
            }
        }
    }
}
