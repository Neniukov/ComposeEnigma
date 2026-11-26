package com.neniukov.composeenigma.ui.sand

import android.graphics.Canvas
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SandMotionTest {
    @Test
    fun `a swing stops requesting frames after settling and can be started again`() = runTest {
        val trail = RecordingTrail()
        val motion = SandMotion(trail)
        val clock = start(motion)
        motion.setSwinging()
        val openingSamples = trail.samples

        frames(clock, 2)
        assertTrue(trail.samples > openingSamples)

        frames(clock, 3_750)
        assertFalse(clock.hasAwaiters)

        val settledSamples = trail.samples
        motion.setSwinging()
        frames(clock, 2)
        assertTrue(trail.samples > settledSamples)
    }

    @Test
    fun `zero scale keeps the opening and a manual launch static`() = runTest {
        val trail = RecordingTrail()
        val motion = SandMotion(trail)
        val openingSamples = trail.samples
        val clock = start(motion, DurationScale(0f))

        frames(clock, 1)
        assertFalse(clock.hasAwaiters)
        assertEquals(openingSamples, trail.samples)

        val openingPosition = trail.position
        motion.setSwinging()
        frames(clock, 1)
        assertFalse(clock.hasAwaiters)
        assertEquals(openingSamples, trail.samples)
        assertNotEquals(openingPosition, trail.position)
    }

    @Test
    fun `at zero scale the stylus stays where it was dropped and raking clears in one frame`() = runTest {
        val trail = RecordingTrail()
        val motion = SandMotion(trail)
        val clock = start(motion, DurationScale(0f))
        frames(clock, 1)
        assertNotNull(motion.tryGrab(trail.position, hitRadius = 0.1f, timeMillis = 1L))

        val dropPosition = Offset(0.45f, 0.3f)
        motion.pull(dropPosition)
        motion.release(Offset(0.7f, 1.5f))
        frames(clock, 1)
        assertFalse(clock.hasAwaiters)

        motion.rake()
        frames(clock, 1)
        assertEquals(1, trail.clears)
        assertEquals(dropPosition, trail.position)
        assertTrue(motion.canHandleTrayInput)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `turning animations off during a rake clears the trail and stops a running swing`() = runTest {
        val trail = RecordingTrail()
        val motion = SandMotion(trail)
        val scale = DurationScale(1f)
        val clock = start(motion, scale)
        frames(clock, 2)

        motion.rake()
        frames(clock, 2)
        assertTrue(motion.raking)
        assertEquals(0, trail.clears)

        scale.scaleFactor = 0f
        frames(clock, 1)
        assertEquals(1, trail.clears)
        assertFalse(motion.raking)
        assertFalse(clock.hasAwaiters)
    }

    private fun TestScope.start(motion: SandMotion, scale: DurationScale = DurationScale(1f)): BroadcastFrameClock {
        val clock = BroadcastFrameClock()
        backgroundScope.launch(clock + scale) { motion.animate() }
        runCurrent()
        return clock
    }

    private fun TestScope.frames(clock: BroadcastFrameClock, count: Int) {
        repeat(count) {
            Snapshot.sendApplyNotifications()
            runCurrent()
            advanceTimeBy(16)
            clock.sendFrame(testScheduler.currentTime * 1_000_000)
            Snapshot.sendApplyNotifications()
            runCurrent()
        }
    }

    private class DurationScale(override var scaleFactor: Float) : MotionDurationScale

    private class RecordingTrail : SandTrail {
        var samples = 0
            private set
        var clears = 0
            private set
        var position = Offset.Zero
            private set

        override fun start(position: Offset) {
            this.position = position
        }

        override fun extend(position: Offset) {
            this.position = position
            samples++
        }

        override fun clear(position: Offset) {
            this.position = position
            clears++
        }

        override fun draw(canvas: Canvas) = error("Frame tests inspect trail updates without drawing")
    }
}
