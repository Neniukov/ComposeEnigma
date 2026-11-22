package com.neniukov.composeenigma.ui.paper

import androidx.compose.ui.graphics.Color
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaperGeometryTest {
    @Test
    fun `house panels keep their edge lengths while folding instead of stretching`() {
        val house = PaperModel().scenery.filter { it.fold != PaperFold.Fixed && it.fold != PaperFold.Cutout }
        assertTrue(house.isNotEmpty())
        for (face in house) {
            for (step in 0..20) {
                val opening = step / 20f
                for (i in face.points.indices) {
                    val a = face.points[i]
                    val b = face.points[(i + 1) % face.points.size]
                    assertEquals(
                        distance(a, b),
                        distance(paperPose(a, face, opening), paperPose(b, face, opening)),
                        0.0002f,
                        "${face.fold} edge $i at $opening",
                    )
                }
            }
        }
    }

    @Test
    fun `the left cover participates in opening instead of staying glued to the stage`() {
        val left = PaperModel().book.first { it.leaf == PaperLeaf.Left }
        val edge = paperPose(PaperPoint(-2f, 0f, 0f), left, 0.5f)
        assertTrue(edge.y > 0.15f)
        assertEquals(0f, paperPose(PaperPoint(-2f, 0f, 0f), left, 1f).y, 0.0001f)
    }

    @Test
    fun `paper cutouts already begin lifting during the first third of opening`() {
        val mountain = PaperModel().scenery.first {
            it.fold == PaperFold.Cutout &&
                it.color != Color.White &&
                it.points.any { p -> p.y > 1.5f }
        }
        val tip = mountain.points.maxBy { it.y }
        val base = tip.copy(y = 0f)
        val lifted = paperPose(tip, mountain, 0.30f).y - paperPose(base, mountain, 0.30f).y
        assertTrue(lifted > 0.05f)
    }

    @Test
    fun `roof and side walls stay attached to the facade throughout opening`() {
        val house = PaperModel().scenery
        val front = house.first { it.fold == PaperFold.Facade && it.castsShadow }
        val roof = house.first { it.fold == PaperFold.Roof && it.foldDirection < 0f }
        val side = house.first { it.fold == PaperFold.SideWall && it.foldDirection < 0f }
        for (step in 0..100) {
            val opening = step / 100f
            // The ridge lip and eave clearances must stay constant instead of drifting apart.
            assertEquals(
                0.02f,
                distance(
                    paperPose(roof.points[1], roof, opening),
                    paperPose(PaperPoint(0f, 0.76f, 0f), front, opening),
                ),
                0.0002f,
            )
            assertEquals(
                0.03f,
                distance(
                    paperPose(roof.points[0], roof, opening),
                    paperPose(PaperPoint(-0.33f, 0.50f, 0f), front, opening),
                ),
                0.0002f,
            )
            assertEquals(
                0f,
                distance(
                    paperPose(side.points[0], side, opening),
                    paperPose(PaperPoint(-0.33f, 0f, 0f), front, opening),
                ),
                0.0002f,
            )
        }
    }

    @Test
    fun `all folded paper pieces lie flat and fit between the closed pages`() {
        val pieces = PaperModel().scenery.filter { it.fold != PaperFold.Fixed }
        for (face in pieces) {
            for (point in face.points) {
                val folded = paperLocalPose(point, face, 0f)
                assertTrue(folded.y in 0f..0.04f, "${face.fold}: $folded")
                assertTrue(folded.z in -1.44f..1.44f, "${face.fold}: $folded")
                val width = if (face.leaf == PaperLeaf.Left) -2.06f..0f else 0f..2.06f
                assertTrue(folded.x in width, "${face.fold}: $folded")
            }
        }
    }

    @Test
    fun `closed pages overlap and fully open pages meet at the spine`() {
        val page = PaperFace(
            listOf(
                PaperPoint(0f, 0f, 0f),
                PaperPoint(2f, 0f, 0f),
                PaperPoint(2f, 0f, 1f),
            ),
            Color.White,
            PaperLeaf.Right,
        )
        val edge = PaperPoint(2f, 0f, 0f)
        val closed = paperPose(edge, page, 0f)
        val open = paperPose(edge, page, 1f)
        val left = PaperModel().book.first { it.leaf == PaperLeaf.Left }
        val closedLeft = paperPose(PaperPoint(-2f, 0f, 0f), left, 0f)
        assertEquals(closedLeft.x, closed.x, 0.0001f)
        assertEquals(closedLeft.y, closed.y, 0.0001f)
        assertEquals(2f, open.x, 0.0001f)
        assertEquals(0f, open.y, 0.0001f)
        for (progress in 0..100) {
            val spine = paperPose(PaperPoint(0f, 0f, 0f), page, progress / 100f)
            assertEquals(0f, spine.x, 0.0001f)
            assertEquals(0f, spine.y, 0.0001f)
            assertEquals(0f, spine.z, 0.0001f)
        }
    }

    @Test
    fun `a mountain folds flat inside the page and stands on the same tab`() {
        val panel = PaperFace(
            listOf(
                PaperPoint(-0.3f, 0f, 0f),
                PaperPoint(0.3f, 0f, 0f),
                PaperPoint(0f, 1.2f, 0f),
            ),
            Color.White,
            fold = PaperFold.Cutout,
            anchor = PaperPoint(-0.7f, 0f, -0.8f),
        )
        val tip = panel.points.last()
        val folded = paperLocalPose(tip, panel, 0f)
        val standing = paperPose(tip, panel, 1f)
        assertEquals(0.025f, folded.y, 0.0001f)
        assertTrue(folded.z in -1.44f..1.44f)
        assertEquals(1.225f, standing.y, 0.0001f)
        assertEquals(-0.8f, standing.z, 0.0001f)
        assertEquals(-0.7f, standing.x)
    }

    @Test
    fun `every paper surface stays finite through a complete opening and reverse`() {
        val model = PaperModel()
        val faces = model.book + model.prints + model.scenery + model.coverFoil
        for (step in 0..100) {
            val opening = step / 100f
            for (face in faces) {
                assertTrue(face.points.size >= 3)
                for (point in face.points) {
                    val pose = paperPose(point, face, opening)
                    assertTrue(pose.x.isFinite() && pose.y.isFinite() && pose.z.isFinite())
                    assertTrue(pose.y > -0.2f && pose.y < 3f)
                }
            }
        }
    }

    private fun distance(a: PaperPoint, b: PaperPoint): Float {
        val x = a.x - b.x
        val y = a.y - b.y
        val z = a.z - b.z
        return sqrt(x * x + y * y + z * z)
    }
}
