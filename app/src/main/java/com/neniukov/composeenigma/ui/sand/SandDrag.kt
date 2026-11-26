package com.neniukov.composeenigma.ui.sand

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker

internal class SandDrag(pointer: Offset, stylus: Offset, timeMillis: Long) {
    private val offset = stylus - pointer
    private val tracker = VelocityTracker().apply { addPosition(timeMillis, stylus) }
    private var lastTime = timeMillis
    private var lastPointer = pointer

    var position = stylus
        private set

    fun move(pointer: Offset, timeMillis: Long): Offset {
        if (timeMillis <= lastTime) return position
        if (timeMillis - lastTime >= IDLE_MILLIS) tracker.resetTracking()
        position = constrainSandPosition(pointer + offset)
        tracker.addPosition(timeMillis, position)
        lastTime = timeMillis
        lastPointer = pointer
        return position
    }

    fun finish(pointer: Offset, timeMillis: Long): Offset {
        if (pointer != lastPointer) {
            if (timeMillis == lastTime) {
                position = constrainSandPosition(pointer + offset)
            } else {
                move(pointer, timeMillis)
            }
        }
        if (timeMillis - lastTime >= IDLE_MILLIS) return Offset.Zero
        val velocity = tracker.calculateVelocity()
        return Offset(velocity.x, velocity.y)
    }

    companion object {
        private const val IDLE_MILLIS = 80L

        fun start(pointer: Offset, stylus: Offset, hitRadius: Float, timeMillis: Long): SandDrag? {
            if ((pointer - stylus).getDistance() > hitRadius) return null
            return SandDrag(pointer, stylus, timeMillis)
        }
    }
}
