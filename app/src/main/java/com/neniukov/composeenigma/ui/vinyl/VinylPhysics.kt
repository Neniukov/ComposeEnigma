package com.neniukov.composeenigma.ui.vinyl

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.exp

internal data class RotorStep(val angle: Float, val velocity: Float)

internal fun advanceRotor(
    angle: Float,
    velocity: Float,
    targetVelocity: Float,
    elapsedSeconds: Float,
): RotorStep {
    val resistance = if (targetVelocity == 0f) 3.8f else 2.6f
    val decay = exp(-resistance * elapsedSeconds)
    val nextVelocity = targetVelocity + (velocity - targetVelocity) * decay
    val displacement = targetVelocity * elapsedSeconds +
        (velocity - targetVelocity) * (1f - decay) / resistance
    return RotorStep(
        angle = normalizeDegrees(angle + displacement),
        velocity = if (abs(nextVelocity - targetVelocity) < 0.1f) targetVelocity else nextVelocity,
    )
}

internal fun normalizeDegrees(angle: Float): Float = ((angle % 360f) + 360f) % 360f

internal fun angularDelta(previous: Float, current: Float): Float = ((current - previous + 540f) % 360f) - 180f

internal fun pointerAngle(x: Float, y: Float): Float = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
