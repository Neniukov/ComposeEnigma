package com.neniukov.composeenigma.ui.sand

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

internal const val SAND_OPENING_SECONDS = 10
private const val EDGE_RADIUS = 0.91f

internal fun constrainSandPosition(target: Offset): Offset {
    val distance = target.getDistance()
    return if (distance > EDGE_RADIUS) target * (EDGE_RADIUS / distance) else target
}

internal class SandPhysics(
    position: Offset = Offset(0.80f, -0.12f),
    velocity: Offset = Offset(0f, 2.75f),
) {
    var position = position
        private set
    var velocity = velocity
        private set

    fun place(target: Offset) {
        position = constrainSandPosition(target)
        velocity = Offset.Zero
    }

    fun push(impulse: Offset) {
        velocity += impulse
    }

    fun release(dragVelocity: Offset) {
        val speed = dragVelocity.getDistance()
        if (!speed.isFinite() || speed <= 0.12f) {
            velocity = Offset.Zero
            return
        }
        val launchSpeed = ((speed - 0.12f) * 0.35f).coerceAtMost(1.55f)
        velocity = dragVelocity * (launchSpeed / speed)

        val radius = position.getDistance()
        if (radius > EDGE_RADIUS - 0.16f) {
            val normal = position / radius
            val outward = (velocity.x * normal.x + velocity.y * normal.y).coerceAtLeast(0f)
            val room = ((EDGE_RADIUS - radius) / 0.16f).coerceIn(0f, 1f)
            velocity -= normal * (outward * (1f - room))
        }
    }

    fun step(seconds: Float): Boolean {
        val decay = exp(-DAMPING * seconds)
        val x = axis(position.x, velocity.x, 2.78f, seconds, decay)
        val y = axis(position.y, velocity.y, 3.76f, seconds, decay)
        position = Offset(x.position, y.position)
        velocity = Offset(x.velocity, y.velocity)

        // The stylus stays inside the rim even after a diagonal flick.
        val distance = position.getDistance()
        if (distance > EDGE_RADIUS) {
            val normal = position / distance
            position = normal * EDGE_RADIUS
            val outward = velocity.x * normal.x + velocity.y * normal.y
            if (outward > 0f) velocity -= normal * outward
        }
        return position.getDistance() + velocity.getDistance() * 0.25f > 0.025f
    }

    private fun axis(position: Float, velocity: Float, frequency: Float, time: Float, decay: Float): Axis {
        val sine = sin(frequency * time)
        val cosine = cos(frequency * time)
        val phase = (velocity + DAMPING * position) / frequency
        val displacement = position * cosine + phase * sine
        val derivative = frequency * (-position * sine + phase * cosine)
        return Axis(decay * displacement, decay * (derivative - DAMPING * displacement))
    }

    private data class Axis(val position: Float, val velocity: Float)

    private companion object {
        const val DAMPING = 0.069f
    }
}
