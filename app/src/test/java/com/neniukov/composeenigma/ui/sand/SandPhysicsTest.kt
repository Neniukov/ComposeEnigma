package com.neniukov.composeenigma.ui.sand

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SandPhysicsTest {
    @Test
    fun `grabbing a stylus next to the rim preserves its position`() {
        val position = Offset(0.9f, 0f)
        val physics = SandPhysics(position)

        physics.place(position)

        assertEquals(position, physics.position)
        assertEquals(Offset.Zero, physics.velocity)
    }

    @Test
    fun `a diagonal pull and flick stay inside the tray`() {
        val physics = SandPhysics()

        physics.place(Offset(1.1f, 1.1f))
        assertEquals(0.91f, physics.position.getDistance(), 0.0001f)
        assertEquals(Offset.Zero, physics.velocity)

        physics.release(Offset(18f, 18f))
        repeat(240) {
            physics.step(1f / 120f)
            assertTrue(physics.position.getDistance() <= 0.9101f)
        }
    }

    @Test
    fun `a held stylus starts its swing from the same place without a launch impulse`() {
        val start = Offset(0.75f, 0f)
        val physics = SandPhysics(start)

        physics.release(Offset(0.07f, 0.02f))
        assertEquals(start, physics.position)
        assertEquals(Offset.Zero, physics.velocity)

        physics.step(1f / 120f)
        assertTrue(physics.position.x < start.x)
        assertTrue((physics.position - start).getDistance() < 0.001f)
    }

    @Test
    fun `a quick release keeps direction with a bounded velocity`() {
        val physics = SandPhysics(Offset.Zero)

        physics.release(Offset(12f, -16f))

        assertEquals(1.55f, physics.velocity.getDistance(), 0.0001f)
        assertTrue(physics.velocity.x > 0f && physics.velocity.y < 0f)
    }

    @Test
    fun `a flick along the rim preserves tangential motion without an outward kick`() {
        val physics = SandPhysics(Offset(0.91f, 0f))

        physics.release(Offset(2f, -2f))

        assertEquals(0f, physics.velocity.x, 0.0001f)
        assertTrue(physics.velocity.y < 0f)
    }

    @Test
    fun `a free swing eventually stops carving`() {
        val physics = SandPhysics()

        repeat(240) {
            assertTrue(physics.step(1f / 120f))
        }
        repeat(60 * 120) {
            physics.step(1f / 120f)
        }

        assertFalse(physics.step(1f / 120f))
    }
}
