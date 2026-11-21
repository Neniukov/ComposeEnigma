package com.neniukov.composeenigma.ui.droid

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DroidMotionTest {
    @Test
    fun `pause freezes an in-flight trick and its clock`() {
        val motion = DroidMotion()
        motion.perform(DroidTrick.Flip)
        repeat(30) { motion.step(1f / 60f) }
        motion.playing = false
        val time = motion.time
        val flip = motion.flip
        val lift = motion.lift

        repeat(120) { motion.step(1f / 60f) }

        assertEquals(time, motion.time)
        assertEquals(flip, motion.flip)
        assertEquals(lift, motion.lift)
    }

    @Test
    fun `a paused robot still turns with a gesture and then settles`() {
        val motion = DroidMotion().apply { playing = false; dragging = true }
        val initialYaw = motion.yaw
        motion.drag(0.25f, 0f)
        repeat(20) { motion.step(1f / 60f) }
        assertTrue(motion.yaw > initialYaw + 0.5f)
        assertEquals(0f, motion.time)

        motion.dragging = false
        repeat(240) { motion.step(1f / 60f) }

        assertFalse(motion.isSettling)
        assertEquals(initialYaw + 1.5f, motion.yaw, 0.001f)
    }

    @Test
    fun `flip takes off rotates and lands upright`() {
        val motion = DroidMotion()
        motion.perform(DroidTrick.Flip)
        repeat(43) { motion.step(1f / 60f) }
        assertTrue(motion.lift > 0.5f)
        assertTrue(motion.flip in 2.5f..3.8f)

        repeat(80) { motion.step(1f / 60f) }

        assertEquals(1f, motion.trickProgress)
        assertEquals(0f, motion.flip)
        assertTrue(abs(motion.lift) < 0.0001f)
    }

    @Test
    fun `repeated commands do not restart or interrupt a trick`() {
        val motion = DroidMotion()
        motion.perform(DroidTrick.Flip)
        repeat(30) { motion.step(1f / 60f) }
        val progress = motion.trickProgress

        motion.perform(DroidTrick.Wave)

        assertEquals(DroidTrick.Flip, motion.trick)
        assertEquals(progress, motion.trickProgress)
        repeat(100) { motion.step(1f / 60f) }
        motion.perform(DroidTrick.Wave)
        assertEquals(DroidTrick.Wave, motion.trick)
        assertEquals(0f, motion.trickProgress)
    }

    @Test
    fun `extreme vertical drags keep the character within viewing limits`() {
        val motion = DroidMotion().apply { playing = false }
        motion.drag(0f, 100f)
        repeat(240) { motion.step(1f / 60f) }
        assertEquals(0.65f, motion.pitch, 0.001f)

        motion.drag(0f, -100f)
        repeat(240) { motion.step(1f / 60f) }
        assertEquals(-0.55f, motion.pitch, 0.001f)
    }
}
