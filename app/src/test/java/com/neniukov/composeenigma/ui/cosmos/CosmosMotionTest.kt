package com.neniukov.composeenigma.ui.cosmos

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.saveable.SaverScope
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
class CosmosMotionTest {
    @Test
    fun `pause freezes both the orbit and an in flight signal`() {
        val motion = CosmosMotion()
        motion.sendSignalFrom(24)
        repeat(30) { motion.step(1f / 60f) }
        motion.togglePlaying()
        val phase = motion.phase
        val signal = motion.signalProgress
        repeat(120) { motion.step(1f / 60f) }
        assertEquals(phase, motion.phase)
        assertEquals(signal, motion.signalProgress)
    }

    @Test
    fun `a paused constellation can still be rotated and stops requesting frames when settled`() = runTest {
        val motion = CosmosMotion(initiallyPlaying = false)
        val clock = BroadcastFrameClock()
        start(motion, clock)
        assertFalse(clock.hasAwaiters)
        val initialYaw = motion.yaw

        // This also exercises the wake-up path used by accessibility rotation actions.
        motion.dragBy(0.2f, 0f)
        frames(clock, 240)
        assertEquals(initialYaw + 1f, motion.yaw, 0.001f)
        assertEquals(0f, motion.phase)
        assertFalse(motion.isSettling)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `a new signal replaces the old wave at the selected node and resumes playback`() {
        val motion = CosmosMotion(initiallyPlaying = false)
        motion.sendSignalFrom(12)
        assertTrue(motion.playing)
        repeat(45) { motion.step(1f / 60f) }
        assertTrue(motion.signalProgress > 0f)
        motion.sendSignalFrom(103)
        assertEquals(103, motion.signalSource)
        assertEquals(0f, motion.signalProgress)
        repeat(240) { motion.step(1f / 60f) }
        assertEquals(1f, motion.signalProgress)
    }

    @Test
    fun `invalid input cannot poison the camera and extreme drags keep the pitch bounded`() {
        val motion = CosmosMotion(initiallyPlaying = false)
        val yaw = motion.yaw
        motion.dragBy(Float.NaN, 0f)
        motion.dragBy(0f, Float.POSITIVE_INFINITY)
        motion.step(Float.NaN)
        motion.step(-1f)
        assertEquals(yaw, motion.yaw)

        motion.dragBy(100f, 100f)
        repeat(240) { motion.step(1f / 60f) }
        assertEquals(1.1f, motion.pitch, 0.001f)
        motion.dragBy(-100f, -100f)
        repeat(240) { motion.step(1f / 60f) }
        assertEquals(-1.1f, motion.pitch, 0.001f)
        assertTrue(motion.yaw.isFinite())
    }

    @Test
    fun `background cancellation releases a gesture and resume does not catch up hidden time`() = runTest {
        val motion = CosmosMotion()
        val clock = BroadcastFrameClock()
        val running = start(motion, clock)
        motion.sendSignal()
        motion.beginDrag()
        motion.dragBy(0.2f, 0f)
        frames(clock, 30)
        running.cancelAndJoin()
        val phase = motion.phase
        val signal = motion.signalProgress
        val yaw = motion.yaw
        assertFalse(motion.dragging)
        frames(clock, 180)
        assertEquals(phase, motion.phase)
        assertEquals(signal, motion.signalProgress)
        assertEquals(yaw, motion.yaw)
        assertFalse(clock.hasAwaiters)

        start(motion, clock)
        frames(clock, 2)
        assertTrue(motion.phase - phase in 0f..0.01f)
        frames(clock, 240)
        assertEquals(1f, motion.signalProgress)
    }

    @Test
    fun `restoring preserves the paused camera and signal with the finger released`() {
        val motion = CosmosMotion()
        motion.sendSignalFrom(84)
        motion.beginDrag()
        motion.dragBy(0.2f, -0.1f)
        repeat(25) { motion.step(1f / 60f) }
        motion.togglePlaying()
        val restored = with(CosmosMotion.Saver) {
            val scope = SaverScope { it is Float || it is Boolean || it is Int || it is String }
            requireNotNull(restore(requireNotNull(scope.save(motion))))
        }
        assertEquals(motion.yaw, restored.yaw)
        assertEquals(motion.pitch, restored.pitch)
        assertEquals(motion.phase, restored.phase)
        assertEquals(motion.signalProgress, restored.signalProgress)
        assertEquals(84, restored.signalSource)
        assertFalse(restored.playing)
        assertFalse(restored.dragging)
        assertFalse(restored.isSettling)
    }

    @Test
    fun `a frame after a long stall advances only by the capped time step`() {
        val motion = CosmosMotion()
        motion.sendSignal()
        motion.step(60f)
        assertTrue(motion.phase in 0f..0.004f)
        assertTrue(motion.signalProgress in 0f..0.012f)
    }

    private fun TestScope.start(motion: CosmosMotion, clock: BroadcastFrameClock): Job {
        val job = backgroundScope.launch(clock) { motion.animate() }
        flush()
        return job
    }

    private fun TestScope.frames(clock: BroadcastFrameClock, count: Int) {
        repeat(count) {
            flush()
            advanceTimeBy(16)
            clock.sendFrame(testScheduler.currentTime * 1_000_000)
            flush()
        }
    }

    private fun TestScope.flush() {
        Snapshot.sendApplyNotifications()
        runCurrent()
    }
}
