package com.neniukov.composeenigma.ui.signal

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

internal const val InitialTuning = 0.28f
internal const val CarrierTuning = 0.70f
internal const val DialSweep = 256f

internal data class SignalFrame(
    val tuning: Float,
    val roll: Float,
    val time: Float,
    val clarity: Float,
    val strength: Float,
    val locked: Boolean,
    val reducedMotion: Boolean,
) {
    val frequencyTenths: Int
        get() = (868f + tuning * 80f).roundToInt()
}

internal class SignalTuner(initialTuning: Float = InitialTuning) {
    private var tuning = initialTuning.coerceIn(0f, 1f)
    private var time = 0f
    private var roll = 0.24f
    private var velocity = 0f
    private var held = false
    private var dwell = 0f
    private var clarity = 0f
    private var locked = false
    private var reducedMotion = false

    val frame: SignalFrame
        get() = SignalFrame(
            tuning = tuning,
            roll = roll,
            time = time,
            clarity = clarity,
            strength = (1f - abs(tuning - CarrierTuning) / 0.38f).coerceIn(0f, 1f),
            locked = locked,
            reducedMotion = reducedMotion,
        )

    fun grab() {
        held = true
        velocity = 0f
    }

    fun turn(delta: Float, seconds: Float) {
        val previous = tuning
        tuning = (tuning + delta).coerceIn(0f, 1f)
        if (seconds > 0f) {
            val speed = ((tuning - previous) / seconds).coerceIn(-1.5f, 1.5f)
            velocity += (speed - velocity) * 0.55f
        }
        updateStationaryLock()
    }

    fun release(canceled: Boolean, allowMomentum: Boolean = false) {
        held = false
        if (canceled || !allowMomentum) velocity = 0f
        if (reducedMotion) {
            if (!canceled && abs(tuning - CarrierTuning) < CaptureRadius) tuning = CarrierTuning
            updateStationaryLock()
        }
    }

    fun seek(position: Float) {
        held = false
        velocity = 0f
        tuning = position.coerceIn(0f, 1f)
        if (reducedMotion && abs(tuning - CarrierTuning) < CaptureRadius) tuning = CarrierTuning
        updateStationaryLock()
    }

    fun reduceMotion(reduce: Boolean) {
        reducedMotion = reduce
        if (reduce) {
            velocity = 0f
            if (!held && abs(tuning - CarrierTuning) < CaptureRadius) tuning = CarrierTuning
            updateStationaryLock()
        }
    }

    fun reset() {
        tuning = InitialTuning
        time = 0f
        roll = 0.24f
        velocity = 0f
        held = false
        dwell = 0f
        clarity = 0f
        locked = false
    }

    fun step(seconds: Float): SignalFrame {
        if (reducedMotion || seconds <= 0f) return frame

        val dt = seconds.coerceAtMost(0.04f)
        if (!held) coast(dt)

        val error = tuning - CarrierTuning
        val inSync = abs(error) <= if (locked) ReleaseRadius else LockRadius
        dwell = if (inSync) (dwell + dt).coerceAtMost(LockDwell) else 0f
        locked = dwell >= LockDwell

        val targetClarity = if (locked) 1f else 0f
        clarity += (targetClarity - clarity) * (1f - exp(-dt * if (locked) 7f else 11f))
        if (locked) {
            // Pull the scan to the nearest frame boundary instead of teleporting to it.
            val towardZero = if (roll > 0.5f) 1f - roll else -roll
            roll = wrap(roll + towardZero * (1f - exp(-dt * 8f)))
        } else {
            val speed = (0.22f + 1.8f * abs(error)) * if (error > 0f) 1f else -1f
            roll = wrap(roll + speed * dt)
        }
        time += dt
        return frame
    }

    private fun coast(dt: Float) {
        val error = tuning - CarrierTuning
        if (abs(error) < CaptureRadius && abs(velocity) < 0.85f) {
            velocity += (-92f * error - 19f * velocity) * dt
        } else {
            velocity *= exp(-dt * 8f)
        }

        val next = tuning + velocity * dt
        tuning = next.coerceIn(0f, 1f)
        if (next != tuning) velocity = 0f
    }

    private fun updateStationaryLock() {
        val radius = if (locked) ReleaseRadius else LockRadius
        if (abs(tuning - CarrierTuning) > radius) {
            locked = false
            dwell = 0f
        }
        if (reducedMotion) {
            locked = abs(tuning - CarrierTuning) <= radius
            dwell = if (locked) LockDwell else 0f
            clarity = if (locked) 1f else 0f
            roll = if (locked) 0f else 0.24f
        }
    }

    private fun wrap(value: Float): Float = (value + 1f) % 1f

    private companion object {
        const val CaptureRadius = 0.094f
        const val LockRadius = 0.026f
        const val ReleaseRadius = 0.048f
        const val LockDwell = 0.36f
    }
}

internal fun shortestDegrees(from: Float, to: Float): Float = (to - from + 540f) % 360f - 180f
