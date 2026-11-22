package com.neniukov.composeenigma.ui.paper

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Shared dimensions keep the model and its folding transforms on the same hinges. */
internal object PaperHouseDimensions {
    const val WALL_HALF_WIDTH = 0.33f
    const val WALL_HEIGHT = 0.50f
    const val ROOF_HALF_WIDTH = 0.36f
    const val ROOF_RISE = 0.28f
    val roofSlope = atan2(ROOF_RISE, ROOF_HALF_WIDTH)
    val roofSpan = sqrt(ROOF_HALF_WIDTH * ROOF_HALF_WIDTH + ROOF_RISE * ROOF_RISE)
}

/** Model-space axes: x across the page, y above it, z from the back to the front. */
internal data class PaperPoint(val x: Float, val y: Float, val z: Float)

internal enum class PaperLeaf {
    Left,
    Right,
}

internal enum class PaperFold {
    Fixed,
    Cutout,
    Facade,
    SideWall,
    Roof,
}

internal class PaperFace(
    val points: List<PaperPoint>,
    val color: Color,
    val leaf: PaperLeaf = PaperLeaf.Left,
    val fold: PaperFold = PaperFold.Fixed,
    val anchor: PaperPoint = PaperPoint(0f, 0f, 0f),
    val start: Float = 0.05f,
    val end: Float = 0.94f,
    val castsShadow: Boolean = false,
    val outline: Boolean = true,
    val foldDirection: Float = 1f,
    val decorationOf: PaperFace? = null,
)

internal class PaperSurface(val face: PaperFace, val details: List<PaperFace>)

/** Details share their supporting panel's paint order rather than their own centroid depth. */
internal fun paperSurfaces(faces: List<PaperFace>): List<PaperSurface> =
    faces.filter { it.decorationOf == null }.map { face ->
        PaperSurface(face, faces.filter { it.decorationOf === face })
    }

internal fun paperPhase(opening: Float, start: Float, end: Float): Float {
    val progress = ((opening - start) / (end - start)).coerceIn(0f, 1f)
    return progress * progress * (3f - 2f * progress)
}

/** Both covers open around the same spine; the closed book leans gently toward the viewer. */
internal fun paperLeafAngle(leaf: PaperLeaf, opening: Float): Float {
    val closedAngle = if (leaf == PaperLeaf.Left) -0.65f else PI.toFloat() - 0.65f
    return closedAngle * (1f - opening)
}

internal fun paperTurnLeaf(point: PaperPoint, leaf: PaperLeaf, opening: Float): PaperPoint {
    val angle = paperLeafAngle(leaf, opening)
    return PaperPoint(
        point.x * cos(angle) - point.y * sin(angle),
        point.x * sin(angle) + point.y * cos(angle),
        point.z,
    )
}

/** Every moving surface is a rigid sheet, with its own crease rather than a height scale. */
internal fun paperLocalPose(point: PaperPoint, face: PaperFace, opening: Float, shadow: Boolean = false): PaperPoint {
    val phase = paperPhase(opening, face.start, face.end)
    val angle = (if (face.fold == PaperFold.Cutout) phase * phase else phase) * PI.toFloat() / 2f
    val direction = face.foldDirection
    val local = when (face.fold) {
        PaperFold.Fixed -> point
        PaperFold.Cutout, PaperFold.Facade -> PaperPoint(
            point.x,
            point.y * sin(angle) - direction * point.z * cos(angle) + 0.025f,
            direction * point.y * cos(angle) + point.z * sin(angle),
        )
        PaperFold.SideWall -> {
            val spread = paperPhase(opening, face.start + 0.02f, face.end) * PI.toFloat() / 2f
            val x = direction * (PaperHouseDimensions.WALL_HALF_WIDTH - point.x * cos(spread) + point.z * sin(spread))
            val z = -point.x * sin(spread) - point.z * cos(spread)
            PaperPoint(
                x,
                point.y * sin(angle) - z * cos(angle) + 0.025f,
                point.y * cos(angle) + z * sin(angle),
            )
        }
        PaperFold.Roof -> {
            // The front roof edge stays attached to the gable throughout both rotations.
            val spread = paperPhase(opening, face.start + 0.06f, face.end) * PI.toFloat() / 2f
            val slope = PaperHouseDimensions.roofSlope
            val x =
                direction *
                    (PaperHouseDimensions.ROOF_HALF_WIDTH - point.x * cos(slope) - point.y * sin(slope) * cos(spread))
            val y = PaperHouseDimensions.WALL_HEIGHT + point.x * sin(slope) - point.y * cos(slope) * cos(spread)
            val z = -point.y * sin(spread)
            PaperPoint(
                x,
                y * sin(angle) - z * cos(angle) + 0.025f,
                y * cos(angle) + z * sin(angle),
            )
        }
    }
    var x = local.x + face.anchor.x
    var y = local.y + face.anchor.y
    var z = local.z + face.anchor.z
    if (shadow) {
        x += y * 0.48f
        z += y * 0.28f
        y = 0.02f
    }
    return PaperPoint(x, y, z)
}

internal fun paperPose(point: PaperPoint, face: PaperFace, opening: Float, shadow: Boolean = false): PaperPoint =
    paperTurnLeaf(paperLocalPose(point, face, opening, shadow), face.leaf, opening)
