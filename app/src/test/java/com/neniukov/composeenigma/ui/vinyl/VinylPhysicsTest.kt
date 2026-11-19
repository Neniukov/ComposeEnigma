package com.neniukov.composeenigma.ui.vinyl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VinylPhysicsTest {
    @Test
    fun `crossing the angle seam preserves the gesture direction`() {
        assertEquals(4f, angularDelta(178f, -178f))
        assertEquals(-4f, angularDelta(-178f, 178f))
    }

    @Test
    fun `motor acceleration is independent of refresh rate`() {
        val sixtyHertz = simulate(60, 200f)
        val oneTwentyHertz = simulate(120, 200f)

        assertEquals(sixtyHertz.angle, oneTwentyHertz.angle, 0.01f)
        assertEquals(sixtyHertz.velocity, oneTwentyHertz.velocity, 0.01f)
    }

    @Test
    fun `reverse momentum slows without changing direction`() {
        var step = RotorStep(180f, -600f)
        repeat(180) {
            step = advanceRotor(step.angle, step.velocity, 0f, 1f / 60f)
            assertTrue(step.velocity <= 0f)
            assertTrue(step.angle in 0f..<360f)
        }

        assertEquals(0f, step.velocity)
    }

    @Test
    fun `a stopped rotor stays exactly still`() {
        assertEquals(RotorStep(143f, 0f), advanceRotor(143f, 0f, 0f, 0.05f))
    }

    @Test
    fun `switching speed keeps the current position continuous`() {
        val step = advanceRotor(125f, 200f, 270f, 1f / 60f)

        assertTrue(step.angle in 128f..129f)
        assertTrue(step.velocity in 200f..210f)
    }

    @Test
    fun `negative and accumulated turns stay within one revolution`() {
        assertEquals(350f, normalizeDegrees(-730f))
        assertEquals(10f, normalizeDegrees(1_090f))
        assertEquals(0f, normalizeDegrees(720f))
    }

    private fun simulate(frames: Int, target: Float): RotorStep {
        var step = RotorStep(0f, 0f)
        repeat(frames) {
            step = advanceRotor(step.angle, step.velocity, target, 1f / frames)
        }
        return step
    }
}
