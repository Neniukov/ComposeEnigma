package com.neniukov.composeenigma.ui.cosmos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs
import kotlin.math.exp

/** Local visual state. Only drawing observes the frame-rate pose and signal progress. */
@Stable
internal class CosmosMotion(
    initialYaw: Float = -0.32f,
    initialPitch: Float = 0.20f,
    initialPhase: Float = 0f,
    initiallyPlaying: Boolean = true,
    initialSource: Int = 53,
    initialSignal: Float = 1f,
) {
    var yaw by mutableFloatStateOf(initialYaw)
        private set
    var pitch by mutableFloatStateOf(initialPitch)
        private set
    var phase by mutableFloatStateOf(initialPhase)
        private set
    var playing by mutableStateOf(initiallyPlaying)
        private set
    var dragging by mutableStateOf(false)
        private set
    var signalSource by mutableIntStateOf(initialSource)
        private set
    var signalProgress by mutableFloatStateOf(initialSignal)
        private set

    private var targetYaw = initialYaw
    private var targetPitch = initialPitch
    private var yawVelocity = 0f
    private var pitchVelocity = 0f
    private var inputRevision by mutableIntStateOf(0)

    val isSettling: Boolean
        get() = abs(targetYaw - yaw) + abs(targetPitch - pitch) +
            abs(yawVelocity) + abs(pitchVelocity) > 0.0005f

    fun togglePlaying() {
        playing = !playing
    }

    fun beginDrag() {
        dragging = true
    }

    fun dragBy(dx: Float, dy: Float) {
        if (!dx.isFinite() || !dy.isFinite()) return
        targetYaw += dx.coerceIn(-1f, 1f) * 5f
        targetPitch = (targetPitch + dy.coerceIn(-1f, 1f) * 3f).coerceIn(-1.1f, 1.1f)
        if (!dragging) inputRevision++
    }

    fun finishDrag() {
        dragging = false
    }

    fun sendSignal() {
        sendSignalFrom((signalSource + 53) % COSMOS_NODE_COUNT)
    }

    fun sendSignalFrom(node: Int) {
        signalSource = node.coerceIn(0, COSMOS_NODE_COUNT - 1)
        signalProgress = 0f
        playing = true
    }

    fun step(deltaSeconds: Float) {
        if (!deltaSeconds.isFinite() || deltaSeconds <= 0f) return
        val dt = deltaSeconds.coerceAtMost(0.032f)
        if (playing) {
            phase = (phase + dt * 0.12f) % COSMOS_TAU
            signalProgress = (signalProgress + dt / 2.8f).coerceAtMost(1f)
        }
        val damping = exp(-12f * dt)
        yawVelocity = (yawVelocity + (targetYaw - yaw) * 95f * dt) * damping
        pitchVelocity = (pitchVelocity + (targetPitch - pitch) * 95f * dt) * damping
        yaw += yawVelocity * dt
        pitch += pitchVelocity * dt
    }

    suspend fun animate() {
        try {
            snapshotFlow { Triple(playing, dragging, inputRevision) }.collectLatest {
                var previousFrame = 0L
                while (playing || isSettling) {
                    withFrameNanos { now ->
                        if (previousFrame != 0L) step((now - previousFrame) / 1_000_000_000f)
                        previousFrame = now
                    }
                }
            }
        } finally {
            finishDrag()
        }
    }

    companion object {
        val Saver = mapSaver<CosmosMotion>(
            save = { motion ->
                mapOf(
                    "yaw" to motion.yaw,
                    "pitch" to motion.pitch,
                    "phase" to motion.phase,
                    "playing" to motion.playing,
                    "source" to motion.signalSource,
                    "signal" to motion.signalProgress,
                )
            },
            restore = { saved ->
                CosmosMotion(
                    initialYaw = saved.getValue("yaw") as Float,
                    initialPitch = saved.getValue("pitch") as Float,
                    initialPhase = saved.getValue("phase") as Float,
                    initiallyPlaying = saved.getValue("playing") as Boolean,
                    initialSource = saved.getValue("source") as Int,
                    initialSignal = saved.getValue("signal") as Float,
                )
            },
        )
    }
}

@Composable
internal fun rememberCosmosMotion(isActive: Boolean): CosmosMotion {
    val motion = rememberSaveable(saver = CosmosMotion.Saver) { CosmosMotion() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(motion, lifecycle, isActive) {
        if (!isActive) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { motion.animate() }
    }
    return motion
}
