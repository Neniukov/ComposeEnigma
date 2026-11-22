package com.neniukov.composeenigma.ui.paper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.neniukov.composeenigma.ui.theme.PaperColors as C

class PaperSurfaceTest {
    @Test
    fun `windows never compete with walls as independently depth sorted surfaces`() {
        val model = PaperModel()
        val surfaces = paperSurfaces(model.scenery)
        assertFalse(surfaces.any { it.face.color == C.Window }, "A wall can otherwise paint over its own window")
        assertEquals(4, surfaces.sumOf { it.details.count { detail -> detail.color == C.Window } })
        assertEquals(model.scenery.size, surfaces.sumOf { 1 + it.details.size })
    }

    @Test
    fun `both front windows the attic window and the door share the facade surface`() {
        val front = paperSurfaces(PaperModel().scenery).single {
            it.face.fold == PaperFold.Facade && it.face.castsShadow
        }
        assertEquals(3, front.details.count { it.color == C.Window })
        assertEquals(1, front.details.count { it.color == C.CoverEdge })
        assertTrue(front.details.all { it.fold == front.face.fold && it.anchor == front.face.anchor })
    }

    @Test
    fun `the side window travels with its own wall during folding and reversing`() {
        val side = paperSurfaces(PaperModel().scenery).single {
            it.face.fold == PaperFold.SideWall &&
                it.face.foldDirection < 0f
        }
        val window = side.details.single { it.color == C.Window }
        assertEquals(side.face.foldDirection, window.foldDirection)
        assertEquals(side.face.anchor, window.anchor)
        for (step in 0..100) {
            val opening = step / 100f
            // The printed window stays 0.008 units above the moving wall plane.
            val point = window.points.first()
            val ink = paperPose(point, window, opening)
            val wall = paperPose(point.copy(z = 0f), side.face, opening)
            val dx = ink.x - wall.x
            val dy = ink.y - wall.y
            val dz = ink.z - wall.z
            assertEquals(0.008f * 0.008f, dx * dx + dy * dy + dz * dz, 0.000001f)
        }
    }
}
