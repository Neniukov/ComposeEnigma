package com.neniukov.composeenigma.ui.sand

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SandDragTest {
    @Test
    fun `only a press near the stylus starts a drag`() {
        val stylus = Offset(0.45f, 0.2f)

        assertNull(SandDrag.start(Offset.Zero, stylus, hitRadius = 0.15f, timeMillis = 100L))
        assertNotNull(SandDrag.start(stylus + Offset(0.1f, 0f), stylus, hitRadius = 0.15f, timeMillis = 100L))
    }

    @Test
    fun `dragging keeps the original offset between the finger and stylus`() {
        val pointer = Offset(0.3f, -0.2f)
        val stylus = Offset(0.55f, -0.15f)
        val drag = SandDrag(pointer, stylus, timeMillis = 100L)
        val movement = Offset(-0.08f, 0.11f)

        assertEquals(stylus, drag.move(pointer, timeMillis = 100L))

        val moved = drag.move(pointer + movement, timeMillis = 116L)
        assertTrue((moved - stylus - movement).getDistance() < 0.0001f)
    }

    @Test
    fun `release uses recent drag movement but drops stale momentum after a hold`() {
        val drag = SandDrag(Offset.Zero, Offset.Zero, timeMillis = 100L)
        repeat(5) { index ->
            drag.move(Offset((index + 1) * 0.04f, 0f), timeMillis = 110L + index * 10L)
        }

        val lastPointer = Offset(5 * 0.04f, 0f)
        assertTrue(drag.finish(lastPointer, timeMillis = 155L).x > 0f)
        assertEquals(Offset.Zero, drag.finish(lastPointer, timeMillis = 250L))
    }

    @Test
    fun `movement on release updates the stylus position and contributes to launch speed`() {
        val stylus = Offset(0.2f, 0f)
        val drag = SandDrag(Offset.Zero, stylus, timeMillis = 100L)
        repeat(4) { index ->
            drag.move(Offset.Zero, timeMillis = 110L + index * 10L)
        }

        val velocity = drag.finish(Offset(0.1f, 0f), timeMillis = 150L)

        assertEquals(0.3f, drag.position.x, 0.0001f)
        assertTrue(velocity.x > 0f)
    }

    @Test
    fun `release still places the stylus when the last two events share a timestamp`() {
        val drag = SandDrag(Offset.Zero, Offset.Zero, timeMillis = 100L)
        drag.move(Offset(0.1f, 0f), timeMillis = 110L)

        val release = Offset(0.2f, 0f)
        drag.finish(release, timeMillis = 110L)

        assertEquals(release, drag.position)
    }

    @Test
    fun `moving the finger farther past the tray rim does not add momentum`() {
        val start = Offset(0.91f, 0f)
        val drag = SandDrag(start, start, timeMillis = 100L)
        repeat(5) { index ->
            val stylus = drag.move(Offset(1f + index * 0.05f, 0f), timeMillis = 110L + index * 10L)
            assertEquals(start.x, stylus.x, 0.0001f)
        }

        assertTrue(drag.finish(Offset(1.2f, 0f), timeMillis = 155L).getDistance() < 0.001f)
    }
}
