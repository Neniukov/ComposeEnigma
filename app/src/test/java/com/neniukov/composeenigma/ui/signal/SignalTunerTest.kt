package com.neniukov.composeenigma.ui.signal

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignalTunerTest {
    @Test
    fun `the signal needs continuous time in range before it locks`() {
        val tuner = SignalTuner(CarrierTuning)
        tuner.grab()

        tuner.advanceFrames(15)
        assertFalse(tuner.frame.locked)
        tuner.turn(.05f, 0f)
        tuner.advanceFrames(1)
        tuner.turn(-.05f, 0f)

        tuner.advanceFrames(15)
        assertFalse(tuner.frame.locked)
        tuner.advanceFrames(9)
        assertTrue(tuner.frame.locked)
    }

    @Test
    fun `a nearby release pulls the dial to the carrier and steadies the picture`() {
        val tuner = SignalTuner(CarrierTuning - .06f)

        tuner.advanceFrames(90)

        assertTrue(tuner.frame.locked)
        assertEquals(CarrierTuning, tuner.frame.tuning, .001f)
        assertTrue(tuner.frame.clarity > .98f)
        assertTrue(tuner.frame.roll < .002f || tuner.frame.roll > .998f)
    }

    @Test
    fun `small adjustments preserve lock but wider detuning releases it`() {
        val tuner = SignalTuner(CarrierTuning)
        tuner.advanceFrames(30)
        tuner.grab()

        tuner.turn(.04f, 0f)
        tuner.advanceFrames(60)
        assertTrue(tuner.frame.locked)

        tuner.turn(.02f, 0f)
        tuner.advanceFrames(30)
        assertFalse(tuner.frame.locked)
        assertTrue(tuner.frame.clarity < .02f)
    }

    @Test
    fun `quick releases coast while a canceled gesture stops at the last position`() {
        val moving = SignalTuner()
        val canceled = SignalTuner()
        for (tuner in listOf(moving, canceled)) {
            tuner.grab()
            tuner.turn(.05f, .03f)
        }
        val releasePoint = moving.frame.tuning
        moving.release(canceled = false, allowMomentum = true)
        canceled.release(canceled = true)

        moving.advanceFrames(24)
        canceled.advanceFrames(24)

        assertTrue(moving.frame.tuning > releasePoint + .04f)
        assertEquals(releasePoint, canceled.frame.tuning)
    }

    @Test
    fun `reduced motion tunes instantly and does not advance the picture clock`() {
        val tuner = SignalTuner()
        tuner.reduceMotion(true)
        tuner.seek(CarrierTuning - .05f)
        val locked = tuner.frame

        assertTrue(locked.locked)
        assertEquals(1f, locked.clarity)
        tuner.advanceFrames(120)
        assertEquals(locked, tuner.frame)

        tuner.seek(0f)
        assertFalse(tuner.frame.locked)
        assertTrue(abs(tuner.frame.roll) > .1f)
    }

    @Test
    fun `turning across the degree seam follows the short arc`() {
        assertEquals(4f, shortestDegrees(178f, -178f))
        assertEquals(-4f, shortestDegrees(-178f, 178f))
    }

    private fun SignalTuner.advanceFrames(count: Int) {
        repeat(count) { step(1f / 60f) }
    }
}
