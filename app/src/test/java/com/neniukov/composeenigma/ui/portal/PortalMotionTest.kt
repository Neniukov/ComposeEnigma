package com.neniukov.composeenigma.ui.portal

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PortalMotionTest {
    @Test
    fun `releasing preserves the current aperture velocity`() = runTest {
        val motion = PortalMotion()
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)

        motion.press(PortalMotion.Home)
        frames(clock, 10)
        val velocity = motion.opening.velocity
        assertTrue(velocity > 0f)

        motion.touching = false
        flushSnapshots()

        assertEquals(velocity, motion.opening.velocity)
    }

    @Test
    fun `retargeting preserves the current movement velocity`() = runTest {
        val motion = PortalMotion()
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)

        motion.press(Offset(0.7f, 0.55f))
        frames(clock, 10)
        val velocity = motion.center.velocity.x
        assertTrue(velocity > 0f)

        motion.target = Offset(0.8f, 0.55f)
        flushSnapshots()

        assertEquals(velocity, motion.center.velocity.x)
    }

    @Test
    fun `small normalized movement is animated before settling`() = runTest {
        val motion = PortalMotion()
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)

        motion.press(PortalMotion.Home + Offset(0.005f, 0f))
        frames(clock, 1)

        assertTrue(motion.center.isRunning)
        frames(clock, 120)
        assertEquals(motion.target, motion.center.value)
    }

    @Test
    fun `completed ripple does not replay when animation restarts`() = runTest {
        val motion = PortalMotion()
        val clock = BroadcastFrameClock()
        val running = startAnimation(motion, clock)

        motion.press(PortalMotion.Home)
        frames(clock, 120)
        motion.touching = false
        frames(clock, 180)
        assertEquals(1f, motion.ripple.value)

        running.cancelAndJoin()
        startAnimation(motion, clock)

        assertEquals(1f, motion.ripple.value)
        assertFalse(motion.ripple.isRunning)
    }

    @Test
    fun `interrupted release is cleared when animation restarts`() = runTest {
        val motion = PortalMotion()
        val clock = BroadcastFrameClock()
        val running = startAnimation(motion, clock)

        motion.press(PortalMotion.Home)
        frames(clock, 120)
        motion.touching = false
        frames(clock, 8)
        assertTrue(motion.ripple.value < 1f)

        running.cancelAndJoin()
        startAnimation(motion, clock)

        assertEquals(0f, motion.opening.value)
        assertEquals(1f, motion.ripple.value)
        assertFalse(motion.touching)
    }

    @Test
    fun `frame clock stays idle after the final release`() = runTest {
        val motion = PortalMotion()
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)

        motion.press(Offset(0.65f, 0.45f))
        frames(clock, 120)
        assertEquals(1f, motion.opening.value)
        assertEquals(motion.target, motion.center.value)

        motion.touching = false
        frames(clock, 180)
        val settledTime = motion.time
        frames(clock, 60)

        assertEquals(0f, motion.opening.value)
        assertEquals(1f, motion.ripple.value)
        assertEquals(settledTime, motion.time)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `reopening interrupts collapse and the last release still completes`() = runTest {
        val motion = PortalMotion()
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)

        motion.press(PortalMotion.Home)
        frames(clock, 60)
        motion.touching = false
        frames(clock, 5)
        motion.press(Offset(0.6f, 0.5f))
        frames(clock, 120)

        assertEquals(1f, motion.opening.value)
        assertEquals(motion.target, motion.center.value)

        motion.touching = false
        frames(clock, 180)

        assertEquals(0f, motion.opening.value)
        assertEquals(1f, motion.ripple.value)
        assertFalse(clock.hasAwaiters)
    }

    private fun TestScope.startAnimation(motion: PortalMotion, clock: BroadcastFrameClock): Job {
        val job = backgroundScope.launch(clock) { animatePortal(motion) }
        flushSnapshots()
        return job
    }

    private fun TestScope.frames(clock: BroadcastFrameClock, count: Int) {
        repeat(count) {
            flushSnapshots()
            advanceTimeBy(16)
            clock.sendFrame(testScheduler.currentTime * 1_000_000)
            flushSnapshots()
        }
    }

    private fun TestScope.flushSnapshots() {
        Snapshot.sendApplyNotifications()
        runCurrent()
    }
}
