package com.neniukov.composeenigma.ui.paper

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
class PaperMotionTest {
    @Test
    fun `a book starts closed and requests no idle frames`() = runTest {
        val motion = PaperMotion()
        val clock = BroadcastFrameClock()
        start(motion, clock)
        frames(clock, 4)
        assertEquals(0f, motion.opening)
        assertFalse(motion.expanded)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `grabbing an opening book retains its pose and follows the finger`() = runTest {
        val motion = PaperMotion()
        val clock = BroadcastFrameClock()
        start(motion, clock)
        motion.setOpen(true)
        frames(clock, 24)
        val pose = motion.opening
        assertTrue(pose > 0f && pose < 1f)

        motion.beginDrag()
        flush()
        assertEquals(pose, motion.opening)
        motion.dragBy(-0.20f)
        flush()
        assertEquals((pose - 0.20f).coerceAtLeast(0f), motion.opening, 0.0001f)
        assertFalse(clock.hasAwaiters)

        motion.finishDrag()
        frames(clock, 240)
        assertEquals(if (motion.expanded) 1f else 0f, motion.opening)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `rapid reversal settles at the latest requested cover position`() = runTest {
        val motion = PaperMotion()
        val clock = BroadcastFrameClock()
        start(motion, clock)
        motion.setOpen(true)
        frames(clock, 35)
        val pose = motion.opening
        motion.setOpen(false)
        flush()
        assertEquals(pose, motion.opening)
        frames(clock, 16)
        assertTrue(motion.opening < pose)
        motion.setOpen(true)
        frames(clock, 240)
        assertEquals(1f, motion.opening)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `background cancellation freezes the scene and resume completes the fold`() = runTest {
        val motion = PaperMotion()
        val clock = BroadcastFrameClock()
        val running = start(motion, clock)
        motion.setOpen(true)
        frames(clock, 28)
        running.cancelAndJoin()
        val pose = motion.opening
        frames(clock, 80)
        assertEquals(pose, motion.opening)
        assertFalse(clock.hasAwaiters)
        start(motion, clock)
        frames(clock, 240)
        assertEquals(1f, motion.opening)
    }

    @Test
    fun `extreme and invalid gestures cannot move the cover outside its hinge limits`() {
        val motion = PaperMotion()
        motion.beginDrag()
        motion.dragBy(100f)
        assertEquals(1f, motion.dragPosition)
        motion.dragBy(Float.NaN)
        motion.dragBy(Float.NEGATIVE_INFINITY)
        assertEquals(1f, motion.dragPosition)
        motion.dragBy(-100f)
        motion.finishDrag()
        assertFalse(motion.expanded)
        assertFalse(motion.dragging)
        assertEquals(0f, motion.dragPosition)
    }

    @Test
    fun `rotation restores a held pose with the gesture released and the nearest target`() {
        val motion = PaperMotion()
        motion.beginDrag()
        motion.dragBy(0.73f)
        val restored = with(PaperMotion.Saver) {
            val scope = SaverScope { it is Float || it is Boolean || it is String }
            requireNotNull(restore(requireNotNull(scope.save(motion))))
        }
        assertEquals(0.73f, restored.opening)
        assertTrue(restored.expanded)
        assertFalse(restored.dragging)
        restored.setOpen(false)
        assertTrue(motion.dragging)
        assertEquals(0.73f, motion.dragPosition)
    }

    @Test
    fun `a button can interrupt a drag without a late release changing its target`() = runTest {
        val motion = PaperMotion()
        val clock = BroadcastFrameClock()
        start(motion, clock)
        motion.beginDrag()
        motion.dragBy(0.73f)
        flush()
        val pose = motion.opening

        motion.setOpen(false)
        motion.dragBy(0.20f)
        motion.finishDrag()
        flush()

        assertFalse(motion.dragging)
        assertFalse(motion.expanded)
        assertEquals(pose, motion.opening)
        frames(clock, 240)
        assertEquals(0f, motion.opening)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun `backgrounding a held book releases the gesture and resumes toward the nearest end`() = runTest {
        val motion = PaperMotion()
        val clock = BroadcastFrameClock()
        val running = start(motion, clock)
        motion.beginDrag()
        motion.dragBy(0.73f)
        flush()
        running.cancelAndJoin()

        assertFalse(motion.dragging)
        assertTrue(motion.expanded)
        assertEquals(0.73f, motion.opening)
        frames(clock, 20)
        assertEquals(0.73f, motion.opening)
        assertFalse(clock.hasAwaiters)

        start(motion, clock)
        frames(clock, 240)
        assertEquals(1f, motion.opening)
        assertFalse(clock.hasAwaiters)
    }

    private fun TestScope.start(motion: PaperMotion, clock: BroadcastFrameClock): Job {
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
