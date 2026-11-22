package com.neniukov.composeenigma.ui.paper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest

/** Owns the visual pose and gesture target; frame-clock work stays in composition. */
@Stable
internal class PaperMotion(initialOpening: Float = 0f, initiallyExpanded: Boolean = false) {
    private val openingAnimation = Animatable(initialOpening.coerceIn(0f, 1f), visibilityThreshold = 0.002f)

    val opening: Float
        get() = openingAnimation.value

    var expanded by mutableStateOf(initiallyExpanded)
        private set
    var dragging by mutableStateOf(false)
        private set
    var dragPosition by mutableFloatStateOf(initialOpening.coerceIn(0f, 1f))
        private set

    fun setOpen(value: Boolean) {
        dragging = false
        expanded = value
    }

    fun beginDrag() {
        dragPosition = opening
        dragging = true
    }

    fun dragBy(fraction: Float) {
        if (dragging && fraction.isFinite()) dragPosition = (dragPosition + fraction).coerceIn(0f, 1f)
    }

    fun finishDrag() {
        if (!dragging) return
        expanded = dragPosition >= 0.5f
        dragging = false
    }

    /** Cancellation releases a held gesture so resuming can settle toward the nearest end. */
    suspend fun animate() {
        try {
            snapshotFlow {
                val target = when {
                    dragging -> dragPosition
                    expanded -> 1f
                    else -> 0f
                }
                dragging to target
            }.collectLatest { (isDragging, target) ->
                if (isDragging) {
                    openingAnimation.snapTo(target)
                } else {
                    openingAnimation.animateTo(target, openingSpring)
                }
            }
        } finally {
            finishDrag()
        }
    }

    companion object {
        private val openingSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 12f,
        )

        val Saver = mapSaver<PaperMotion>(
            save = { motion ->
                mapOf(
                    "opening" to if (motion.dragging) motion.dragPosition else motion.opening,
                    "expanded" to if (motion.dragging) (motion.dragPosition >= 0.5f) else motion.expanded,
                )
            },
            restore = { saved ->
                PaperMotion(saved.getValue("opening") as Float, saved.getValue("expanded") as Boolean)
            },
        )
    }
}

@Composable
internal fun rememberPaperMotion(isActive: Boolean): PaperMotion {
    val motion = rememberSaveable(saver = PaperMotion.Saver) { PaperMotion() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(motion, lifecycle, isActive) {
        if (!isActive) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { motion.animate() }
    }
    return motion
}
