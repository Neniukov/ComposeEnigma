package com.neniukov.composeenigma.ui.vinyl

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.snapshots.Snapshot
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
class VinylMotionTest {
    @Test
    fun `retargeting the needle preserves its current velocity`() = runTest {
        val motion = VinylMotion(1f, 0f)
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)
        frames(clock, 8)
        val velocity = motion.arm.velocity
        assertTrue(velocity > 0f)

        motion.togglePlayback()
        flushSnapshots()

        assertEquals(velocity, motion.arm.velocity)
    }

    @Test
    fun `the needle waits until the record is seated`() = runTest {
        val motion = VinylMotion()
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)

        frames(clock, 60)
        assertTrue(motion.placement.value < 1f)
        assertEquals(0f, motion.arm.value)
        assertEquals(0f, motion.velocity)
        assertFalse(motion.grab())

        frames(clock, 360)
        assertEquals(1f, motion.placement.value)
        assertEquals(1f, motion.arm.value)
        assertEquals(200f, motion.velocity)
    }

    @Test
    fun `holding the record interrupts the motor without moving it`() = runTest {
        val motion = VinylMotion(1f, 1f)
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)
        frames(clock, 120)

        assertTrue(motion.grab())
        val heldAngle = motion.angle
        frames(clock, 60)

        assertEquals(heldAngle, motion.angle)
        assertEquals(0f, motion.velocity)
        assertFalse(clock.hasAwaiters)

        motion.release(0f)
        frames(clock, 240)
        assertEquals(200f, motion.velocity)
    }

    @Test
    fun `a paused record accepts a reverse fling and settles completely`() = runTest {
        val motion = VinylMotion(1f, 0f)
        motion.togglePlayback()
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)
        frames(clock, 2)

        assertTrue(motion.grab())
        motion.dragBy(-25f)
        motion.release(-600f)
        frames(clock, 20)
        assertTrue(motion.velocity < 0f)

        frames(clock, 180)
        val restingAngle = motion.angle
        frames(clock, 60)
        assertEquals(0f, motion.velocity)
        assertEquals(restingAngle, motion.angle)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `pausing during extraction does not restart the placement animation`() = runTest {
        val motion = VinylMotion()
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)
        frames(clock, 60)

        motion.togglePlayback()
        frames(clock, 50)

        assertEquals(1f, motion.placement.value)
        assertEquals(0f, motion.arm.value)
        assertEquals(0f, motion.velocity)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `eject lifts the needle before returning the record to its sleeve`() = runTest {
        val motion = VinylMotion(1f, 1f)
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)
        frames(clock, 2)

        motion.eject()
        frames(clock, 8)
        assertTrue(motion.arm.value > 0f)
        assertEquals(1f, motion.placement.value)

        frames(clock, 240)
        assertEquals(0f, motion.arm.value)
        assertEquals(0f, motion.placement.value)
        assertEquals(0f, motion.velocity)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `eject invalidates the release of an interrupted gesture`() {
        val motion = VinylMotion(1f, 1f)
        assertTrue(motion.grab())
        motion.dragBy(45f)

        motion.eject()
        motion.release(900f)

        assertFalse(motion.touching)
        assertEquals(0f, motion.velocity)
        assertEquals(0f, motion.motorVelocity)
        assertFalse(motion.spin(500f))
    }

    @Test
    fun `rapid eject and reload finishes in the latest requested state`() = runTest {
        val motion = VinylMotion(1f, 1f)
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)

        motion.eject()
        frames(clock, 70)
        assertTrue(motion.placement.value < 1f)

        motion.togglePlayback()
        frames(clock, 360)

        assertTrue(motion.loaded)
        assertTrue(motion.playing)
        assertEquals(1f, motion.placement.value)
        assertEquals(1f, motion.arm.value)
        assertEquals(200f, motion.velocity)
    }

    @Test
    fun `lifecycle cancellation clears a held gesture and preserves the angle`() = runTest {
        val motion = VinylMotion(1f, 1f)
        val clock = BroadcastFrameClock()
        val running = startAnimation(motion, clock)
        frames(clock, 60)
        assertTrue(motion.grab())
        motion.dragBy(40f)
        val heldAngle = motion.angle

        running.cancelAndJoin()
        frames(clock, 60)
        assertFalse(motion.touching)
        assertEquals(heldAngle, motion.angle)
        assertEquals(0f, motion.velocity)
        assertFalse(clock.hasAwaiters)

        startAnimation(motion, clock)
        assertEquals(heldAngle, motion.angle)
        frames(clock, 240)
        assertEquals(200f, motion.velocity)
    }

    @Test
    fun `switching to a single accelerates to forty five rpm`() = runTest {
        val motion = VinylMotion(1f, 1f)
        val clock = BroadcastFrameClock()
        startAnimation(motion, clock)
        frames(clock, 240)
        assertEquals(200f, motion.velocity)

        motion.selectSpeed(VinylSpeed.Single)
        frames(clock, 240)

        assertEquals(270f, motion.velocity)
    }

    @Test
    fun `release velocity is bounded and nonfinite input cannot poison the rotor`() {
        val motion = VinylMotion(1f, 0f)
        assertTrue(motion.grab())
        motion.dragBy(Float.NaN)
        motion.release(Float.POSITIVE_INFINITY)
        assertEquals(0f, motion.angle)
        assertEquals(0f, motion.velocity)

        assertTrue(motion.spin(-9_000f))
        assertEquals(-1_080f, motion.velocity)
    }

    private fun TestScope.startAnimation(motion: VinylMotion, clock: BroadcastFrameClock): Job {
        val job = backgroundScope.launch(clock) { animateVinyl(motion) }
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
