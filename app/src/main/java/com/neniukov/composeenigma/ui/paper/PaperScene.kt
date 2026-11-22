package com.neniukov.composeenigma.ui.paper

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import com.neniukov.composeenigma.ui.theme.PaperColors as C

@Composable
internal fun PaperScene(opening: () -> Float, modifier: Modifier = Modifier) {
    val renderer = remember { PaperRenderer() }
    Canvas(modifier.clipToBounds()) { with(renderer) { render(opening().coerceIn(0f, 1f)) } }
}

@Composable
internal fun PaperCover(modifier: Modifier = Modifier) {
    val renderer = remember { PaperRenderer() }
    Canvas(modifier.clipToBounds()) {
        drawRect(C.Background)
        with(renderer) { render(1f, cover = true) }
    }
}

private class ProjectedPaper(val face: PaperFace, val details: List<ProjectedPaper> = emptyList()) {
    val path = Path()
    val shadow = Path()
    val world = FloatArray(face.points.size * 3)
    var depth = 0f
    var shade = 1f
}

/** Cached paper geometry, perspective projection and a back-to-front stack of cutouts. */
private class PaperRenderer {
    private val model = PaperModel()
    private val book = model.book.map { ProjectedPaper(it) }
    private val prints = model.prints.map { ProjectedPaper(it) }
    private val scenery = paperSurfaces(model.scenery).map { surface ->
        ProjectedPaper(surface.face, surface.details.map { ProjectedPaper(it) })
    }
    private val foil = model.coverFoil.map { ProjectedPaper(it) }
    private val sorted = ArrayList<ProjectedPaper>(scenery.size + book.size)
    private val bookByLeaf = PaperLeaf.entries.associateWith { leaf -> book.filter { it.face.leaf == leaf } }
    private val printsByLeaf = PaperLeaf.entries.associateWith { leaf ->
        (prints + scenery.filter { it.face.fold == PaperFold.Fixed }).filter { it.face.leaf == leaf }
    }
    private val sceneryByLeaf = PaperLeaf.entries.associateWith { leaf ->
        scenery.filter { it.face.leaf == leaf && it.face.fold != PaperFold.Fixed }
    }
    private val leafClips = PaperLeaf.entries.associateWith { leaf ->
        val x1 = if (leaf == PaperLeaf.Left) -2.06f else 0.01f
        val x2 = if (leaf == PaperLeaf.Left) -0.01f else 2.06f
        ProjectedPaper(
            PaperFace(
                listOf(
                    PaperPoint(x1, 0.02f, -1.43f),
                    PaperPoint(x2, 0.02f, -1.43f),
                    PaperPoint(x2, 0.02f, 1.43f),
                    PaperPoint(x1, 0.02f, 1.43f),
                ),
                C.Page,
                leaf,
            ),
        )
    }
    private val shadowCasters = PaperLeaf.entries.associateWith { leaf ->
        scenery.filter { it.face.leaf == leaf && it.face.castsShadow }
    }
    private val weave = buildList {
        repeat(52) { i ->
            val x = 0.05f + i * 0.039f
            add(PaperPoint(x, -0.168f, -1.47f) to PaperPoint(x, -0.168f, 1.47f))
        }
        repeat(68) { i ->
            val z = -1.46f + i * 0.043f
            add(PaperPoint(0.05f, -0.168f, z) to PaperPoint(2.07f, -0.168f, z))
        }
    }
    private val coverPlane = model.coverFoil.first()
    private var scale = 1f
    private var origin = Offset.Zero
    private var recenterX = 0f
    private var recenterY = 0f
    private val yawCos = cos(-0.30f)
    private val yawSin = sin(-0.30f)
    private val elevationCos = cos(0.78f)
    private val elevationSin = sin(0.78f)

    fun DrawScope.render(opening: Float, cover: Boolean = false) {
        scale = min(size.width / 5.35f, size.height / 4.60f) * (1f + (1f - opening) * 0.18f)
        origin = Offset(size.width * 0.50f, size.height * if (cover) 0.72f else (0.51f + opening * 0.07f))
        val leftCenter = paperTurnLeaf(PaperPoint(-1.06f, 0f, 0f), PaperLeaf.Left, opening)
        val rightCenter = paperTurnLeaf(PaperPoint(1.06f, 0f, 0f), PaperLeaf.Right, opening)
        recenterX = -(leftCenter.x + rightCenter.x) * 0.5f
        recenterY = -(leftCenter.y + rightCenter.y) * 0.5f
        val ground = origin + Offset(scale * 0.12f, scale * 0.72f)
        val shadowWidth = scale * (2.9f + opening * 1.8f)
        drawOval(
            Brush.radialGradient(listOf(C.Shadow.copy(alpha = 0.13f), Color.Transparent), ground, shadowWidth / 2),
            ground - Offset(shadowWidth / 2, scale * 0.57f),
            Size(shadowWidth, scale * 1.14f),
        )

        // Paint each leaf together with its contents, far to near, so the cover occludes the artwork.
        val leftDepth = cameraDepth(paperTurnLeaf(PaperPoint(-1.06f, -0.08f, 0f), PaperLeaf.Left, opening))
        val rightDepth = cameraDepth(paperTurnLeaf(PaperPoint(1.06f, -0.08f, 0f), PaperLeaf.Right, opening))
        if (leftDepth < rightDepth) {
            renderLeaf(PaperLeaf.Left, opening)
            renderLeaf(PaperLeaf.Right, opening)
        } else {
            renderLeaf(PaperLeaf.Right, opening)
            renderLeaf(PaperLeaf.Left, opening)
        }
    }

    private fun DrawScope.renderLeaf(leaf: PaperLeaf, opening: Float) {
        sorted.clear()
        bookByLeaf.getValue(leaf).forEach {
            prepare(it, opening)
            sorted.add(it)
        }
        sorted.sortBy { it.depth }
        sorted.forEach { drawFace(it) }

        val angle = paperLeafAngle(leaf, opening)
        val insideFacing = -sin(angle) * yawSin * elevationCos + cos(angle) * elevationSin
        if (insideFacing <= 0f) {
            if (leaf == PaperLeaf.Right) drawFoil(opening)
            return
        }

        val clip = leafClips.getValue(leaf)
        prepare(clip, opening)
        clipPath(clip.path) {
            printsByLeaf.getValue(leaf).forEach {
                prepare(it, opening)
                drawPath(it.path, it.face.color)
            }
            shadowCasters.getValue(leaf).forEach { face ->
                preparePath(face.shadow, face.face, opening, shadow = true)
                val alpha = paperPhase(opening, face.face.start, face.face.end) * 0.105f
                drawPath(face.shadow, C.Shadow.copy(alpha = alpha * 0.24f), style = Stroke(scale * 0.055f))
                drawPath(face.shadow, C.Shadow.copy(alpha = alpha))
            }
        }
        sorted.clear()
        sceneryByLeaf.getValue(leaf).forEach { face ->
            prepare(face, opening)
            // Cutouts keep their page order; the house panels share a layer and sort by camera depth.
            val stack = if (face.face.fold == PaperFold.Cutout) face.face.anchor.z else 0.40f
            face.depth = stack + face.depth * 0.015f
            sorted.add(face)
        }
        sorted.sortBy { it.depth }
        sorted.forEach { surface ->
            drawFace(surface)
            if (surface.details.isNotEmpty()) {
                // Printed details belong to this sheet. Sorting their centroids independently
                // lets a wall overpaint its own windows when the book changes angle.
                clipPath(surface.path) {
                    surface.details.forEach { detail ->
                        prepare(detail, opening)
                        drawFace(detail)
                    }
                }
            }
        }
    }

    private fun DrawScope.drawFoil(opening: Float) {
        weave.forEach { (start, end) ->
            drawLine(
                C.PageLight.copy(alpha = 0.045f),
                project(paperPose(start, coverPlane, opening)),
                project(paperPose(end, coverPlane, opening)),
                0.55f,
            )
        }
        foil.forEachIndexed { index, face ->
            prepare(face, opening)
            if (!face.face.outline) {
                drawPath(face.path, C.Gold)
            } else {
                drawPath(
                    face.path,
                    C.Gold.copy(alpha = 0.85f),
                    style = Stroke(
                        scale * if (index == 0) 0.009f else 0.015f,
                    ),
                )
            }
        }
    }

    private fun project(point: PaperPoint): Offset {
        val x = point.x + recenterX
        val y = point.y + recenterY
        val horizontal = x * yawCos - point.z * yawSin
        val z = x * yawSin + point.z * yawCos
        val vertical = y * elevationCos - z * elevationSin
        val depth = y * elevationSin + z * elevationCos
        val perspective = 9f / (9f - depth)
        return origin + Offset(horizontal * perspective * scale, -vertical * perspective * scale)
    }

    private fun cameraDepth(point: PaperPoint): Float =
        point.y * elevationSin + (point.x * yawSin + point.z * yawCos) * elevationCos

    private fun preparePath(path: Path, face: PaperFace, opening: Float, shadow: Boolean = false) {
        path.reset()
        face.points.forEachIndexed { index, point ->
            val p = project(paperPose(point, face, opening, shadow))
            if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
        path.close()
    }

    private fun prepare(projected: ProjectedPaper, opening: Float) {
        val face = projected.face
        projected.path.reset()
        projected.depth = 0f
        face.points.forEachIndexed { index, point ->
            val world = paperPose(point, face, opening)
            projected.world[index * 3] = world.x
            projected.world[index * 3 + 1] = world.y
            projected.world[index * 3 + 2] = world.z
            val p = project(world)
            if (index == 0) projected.path.moveTo(p.x, p.y) else projected.path.lineTo(p.x, p.y)
            projected.depth += cameraDepth(world)
        }
        projected.path.close()
        projected.depth /= face.points.size
        val world = projected.world
        val ax = world[3] - world[0]
        val ay = world[4] - world[1]
        val az = world[5] - world[2]
        val bx = world[6] - world[0]
        val by = world[7] - world[1]
        val bz = world[8] - world[2]
        val nx = ay * bz - az * by
        val ny = az * bx - ax * bz
        val nz = ax * by - ay * bx
        val length = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.00001f)
        projected.shade = 0.82f + abs((-nx * 0.35f + ny * 0.82f + nz * 0.45f) / length).coerceIn(0f, 1f) * 0.18f
    }

    private fun DrawScope.drawFace(projected: ProjectedPaper) {
        val color = projected.face.color
        val shade = projected.shade
        drawPath(
            projected.path,
            color.copy(
                red = color.red * shade,
                green = color.green * shade,
                blue =
                color.blue * shade,
            ),
        )
        if (projected.face.outline) {
            drawPath(projected.path, C.Snow.copy(alpha = 0.34f), style = Stroke((scale * 0.008f).coerceAtLeast(0.6f)))
        }
    }
}
