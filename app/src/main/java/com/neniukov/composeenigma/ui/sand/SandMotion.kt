package com.neniukov.composeenigma.ui.sand

import android.graphics.Canvas
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.first
import kotlin.coroutines.coroutineContext

@Stable
internal class SandMotion(private val trail: SandTrail = CanvasSandTrail()) {
    private val physics = SandPhysics()
    private var revision by mutableIntStateOf(0)
    private var playing by mutableStateOf(true)
    private var nextLaunch = 0
    private var drawnPosition = physics.position
    private var previousStepPosition = physics.position
    private var frameRemainder = 0f
    private var rakeProgress = 0f

    var held by mutableStateOf(false)
        private set
    var raking by mutableStateOf(false)
        private set

    val canHandleTrayInput: Boolean
        get() = !held && !raking

    private val needsFrame: Boolean
        get() = raking || (playing && !held)

    init {
        trail.start(physics.position)
        repeat(SAND_OPENING_SECONDS * SAMPLES_PER_SECOND) { tick() }
        alignDrawingToPhysics()
    }

    fun tryGrab(pointer: Offset, hitRadius: Float, timeMillis: Long): SandDrag? {
        if (!canHandleTrayInput) return null
        val drag = SandDrag.start(pointer, drawnPosition, hitRadius, timeMillis) ?: return null
        held = true
        physics.place(drawnPosition)
        alignDrawingToPhysics()
        return drag
    }

    fun pull(target: Offset) {
        physics.place(target)
        alignDrawingToPhysics()
    }

    fun release(dragVelocity: Offset) {
        if (!held) return
        held = false
        physics.release(dragVelocity)
        trail.start(physics.position)
        playing = true
    }

    fun cancelGrab() {
        if (!held) return
        held = false
        trail.start(physics.position)
    }

    fun setSwinging() {
        if (!canHandleTrayInput) return
        val launch = Launches[nextLaunch]
        nextLaunch = (nextLaunch + 1) % Launches.size
        physics.place(launch.position)
        physics.push(launch.velocity)
        trail.start(physics.position)
        playing = true
        alignDrawingToPhysics()
    }

    fun rake() {
        if (!canHandleTrayInput) return
        rakeProgress = 0f
        raking = true
    }

    fun draw(canvas: Canvas, artwork: SandArtwork, lift: Float) {
        // Reading the revision invalidates only the canvas.
        @Suppress("UNUSED_VARIABLE")
        val drawRevision = revision
        artwork.drawTray(canvas)
        trail.draw(canvas)
        if (raking) artwork.drawSweep(canvas, rakeProgress)
        artwork.drawStylus(canvas, drawnPosition, lift)
    }

    suspend fun animate() {
        val durationScale = coroutineContext[MotionDurationScale]
        while (true) {
            snapshotFlow { needsFrame }.first { it }
            var previous = 0L
            while (needsFrame) {
                withFrameNanos { now ->
                    val seconds = if (previous == 0L) {
                        0f
                    } else {
                        ((now - previous) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    }
                    advanceFrame(seconds, durationScale?.scaleFactor ?: 1f)
                    previous = now
                }
            }
        }
    }

    private fun advanceFrame(seconds: Float, durationScale: Float) {
        if (durationScale == 0f) {
            if (playing && !held) {
                physics.place(drawnPosition)
                stopSwing()
            }
            if (raking) finishRake()
            return
        }
        if (raking) {
            rakeProgress += seconds / (RAKE_SECONDS * durationScale)
            if (rakeProgress >= 1f) {
                finishRake()
            } else {
                revision++
            }
            return
        }
        if (!playing || held || seconds <= 0f) return
        frameRemainder += seconds
        while (frameRemainder >= FRAME_STEP) {
            val moving = tick()
            frameRemainder -= FRAME_STEP
            if (!moving) {
                stopSwing()
                return
            }
        }
        val fraction = frameRemainder / FRAME_STEP
        drawnPosition = previousStepPosition + (physics.position - previousStepPosition) * fraction
        revision++
    }

    private fun finishRake() {
        trail.clear(physics.position)
        raking = false
        revision++
    }

    private fun stopSwing() {
        playing = false
        alignDrawingToPhysics()
    }

    private fun alignDrawingToPhysics() {
        drawnPosition = physics.position
        previousStepPosition = drawnPosition
        frameRemainder = 0f
        revision++
    }

    private fun tick(): Boolean {
        previousStepPosition = physics.position
        val moving = physics.step(FRAME_STEP)
        trail.extend(physics.position)
        return moving
    }

    private data class Launch(val position: Offset, val velocity: Offset)

    private companion object {
        const val SAMPLES_PER_SECOND = 120
        const val FRAME_STEP = 1f / SAMPLES_PER_SECOND
        const val RAKE_SECONDS = 0.9f
        val Launches = listOf(
            Launch(Offset(-0.69f, 0.25f), Offset(-0.12f, -2.44f)),
            Launch(Offset(0.38f, 0.72f), Offset(-1.91f, 0.12f)),
            Launch(Offset(0.74f, -0.31f), Offset(0.15f, 2.17f)),
        )
    }
}
