package com.neniukov.composeenigma.ui.vinyl

import androidx.compose.runtime.saveable.SaverScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class VinylMotionSaverTest {
    @Test
    fun `a paused record retains its selected speed and seated position`() {
        VinylSpeed.entries.forEach { speed ->
            val motion = VinylMotion(initialPlacement = 1f)
            motion.togglePlayback()
            motion.selectSpeed(speed)

            val restored = roundTrip(motion)

            assertTrue(restored.loaded)
            assertFalse(restored.playing)
            assertEquals(speed, restored.speed)
            assertEquals(1f, restored.placement.value)
            assertEquals(0f, restored.arm.value)
            assertEquals(0f, restored.motorVelocity)
        }
    }

    @Test
    fun `an ejected record stays in its sleeve with its selected speed`() {
        val motion = VinylMotion()
        motion.selectSpeed(VinylSpeed.Single)
        motion.eject()

        val restored = roundTrip(motion)

        assertFalse(restored.loaded)
        assertFalse(restored.playing)
        assertEquals(VinylSpeed.Single, restored.speed)
        assertEquals(0f, restored.placement.value)
        assertEquals(0f, restored.arm.value)
        assertFalse(restored.canTouch)
    }

    @Test
    fun `an active record retains playback without restoring angular momentum`() {
        val motion = VinylMotion(initialPlacement = 1f, initialArm = 1f)
        motion.selectSpeed(VinylSpeed.Single)
        assertTrue(motion.spin(800f))
        motion.advance(0.05f)
        val angle = motion.angle
        val velocity = motion.velocity

        val restored = roundTrip(motion)

        assertTrue(restored.loaded)
        assertTrue(restored.playing)
        assertEquals(1f, restored.placement.value)
        assertEquals(1f, restored.arm.value)
        assertEquals(angle, restored.angle)
        assertEquals(0f, restored.velocity)
        assertEquals(270f, restored.motorVelocity)
        assertEquals(velocity, motion.velocity)
    }

    @Test
    fun `a held record restores its angle with the gesture released`() {
        val motion = VinylMotion(initialPlacement = 1f, initialArm = 1f)
        assertTrue(motion.grab())
        motion.dragBy(-45f)

        val restored = roundTrip(motion)

        assertEquals(315f, restored.angle)
        assertFalse(restored.touching)
        assertEquals(0f, restored.velocity)
        assertTrue(motion.touching)
        assertEquals(315f, motion.angle)
    }

    @Test
    fun `interrupted extraction retains its pose and paused target`() {
        val motion = VinylMotion(initialPlacement = 0.42f)
        motion.togglePlayback()

        val restored = roundTrip(motion)

        assertEquals(0.42f, restored.placement.value)
        assertEquals(0f, restored.arm.value)
        assertTrue(restored.loaded)
        assertFalse(restored.playing)
        assertFalse(restored.canTouch)
    }

    @Test
    fun `interrupted ejection retains its pose and unloaded target`() {
        val motion = VinylMotion(initialPlacement = 1f, initialArm = 0.5f)
        motion.eject()

        val restored = roundTrip(motion)

        assertEquals(1f, restored.placement.value)
        assertEquals(0.5f, restored.arm.value)
        assertFalse(restored.loaded)
        assertFalse(restored.playing)
    }

    @Test
    fun `restored state is independent of the original holder`() {
        val motion = VinylMotion(initialPlacement = 1f, initialArm = 1f)

        val restored = roundTrip(motion)
        restored.eject()
        restored.selectSpeed(VinylSpeed.Single)

        assertNotSame(motion, restored)
        assertNotSame(motion.placement, restored.placement)
        assertNotSame(motion.arm, restored.arm)
        assertTrue(motion.loaded)
        assertTrue(motion.playing)
        assertEquals(VinylSpeed.LongPlay, motion.speed)
    }

    private fun roundTrip(motion: VinylMotion): VinylMotion = with(VinylMotion.Saver) {
        val scope = SaverScope { it is Boolean || it is String || it is Float }
        val saved = requireNotNull(scope.save(motion))
        requireNotNull(restore(saved))
    }
}
