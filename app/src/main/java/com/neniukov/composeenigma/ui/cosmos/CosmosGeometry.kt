package com.neniukov.composeenigma.ui.cosmos

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal const val COSMOS_NODE_COUNT = 144
internal const val COSMOS_TAU = (PI * 2).toFloat()

/** Unit sphere coordinates: x right, y up, z toward the viewer. */
internal data class CosmosPoint(val x: Float, val y: Float, val z: Float) {
    fun distanceTo(other: CosmosPoint): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

internal data class CosmosLink(val from: Int, val to: Int)

internal data class CosmosProjection(val x: Float, val y: Float, val depth: Float, val perspective: Float)

internal class CosmosViewport(val width: Float, val height: Float) {
    val radius = minOf(width * 0.345f, height * 0.34f)
    val centerX = width * 0.5f
    val centerY = height * 0.50f

    fun project(point: CosmosPoint): CosmosProjection {
        val perspective = 4.5f / (4.5f - point.z)
        return CosmosProjection(
            centerX + point.x * radius * perspective,
            centerY - point.y * radius * perspective,
            point.z,
            perspective,
        )
    }
}

/** A Fibonacci sphere avoids poles and latitude bands; links are undirected nearest neighbours. */
internal class CosmosModel {
    val nodes = List(COSMOS_NODE_COUNT) { index ->
        val y = 1f - 2f * (index + 0.5f) / COSMOS_NODE_COUNT
        val radius = sqrt(1f - y * y)
        val angle = index * (PI * (3.0 - sqrt(5.0))).toFloat()
        CosmosPoint(cos(angle) * radius, y, sin(angle) * radius)
    }

    val links = buildList {
        val seen = mutableSetOf<Pair<Int, Int>>()
        nodes.forEachIndexed { index, point ->
            nodes.indices.asSequence()
                .filter { it != index }
                .sortedBy { point.distanceTo(nodes[it]) }
                .take(4)
                .forEach { neighbour ->
                    val ends = minOf(index, neighbour) to maxOf(index, neighbour)
                    if (seen.add(ends)) add(CosmosLink(ends.first, ends.second))
                }
        }
    }

    val orbits = List(3) { orbit ->
        List(161) { step ->
            cosmosOrbitPoint(step / 160f * COSMOS_TAU, orbit)
        }
    }

    val filaments = List(2) { strand ->
        List(145) { step ->
            val angle = step / 144f * COSMOS_TAU
            val phase = strand * COSMOS_TAU / 2f
            val radius = 0.38f + 0.10f * cos(3f * angle + phase)
            CosmosPoint(
                radius * cos(2f * angle + phase),
                0.48f * sin(3f * angle + phase),
                radius * sin(2f * angle + phase),
            )
        }
    }
}

internal fun cosmosOrbitPoint(angle: Float, orbit: Int): CosmosPoint {
    val radius = 1.26f + orbit * 0.07f
    val tilt = when (orbit) {
        0 -> 0.32f
        1 -> 1.02f
        else -> -0.82f
    }
    val point = CosmosPoint(cos(angle) * radius, sin(angle) * radius, 0f)
    return cosmosRotate(point, yaw = 0.30f + orbit * 0.80f, pitch = tilt)
}

internal fun cosmosRotate(point: CosmosPoint, yaw: Float, pitch: Float): CosmosPoint {
    val x = point.x * cos(yaw) + point.z * sin(yaw)
    val z = -point.x * sin(yaw) + point.z * cos(yaw)
    return CosmosPoint(x, point.y * cos(pitch) - z * sin(pitch), point.y * sin(pitch) + z * cos(pitch))
}

/** A finite wave front travels across chord distance on the sphere, then fades completely. */
internal fun cosmosSignalEnergy(distance: Float, progress: Float): Float {
    if (progress !in 0f..1f || distance !in 0f..2.001f) return 0f
    val wave = (1f - abs(distance - progress * 2.4f) / 0.23f).coerceIn(0f, 1f)
    return wave * wave * (1f - progress)
}

internal fun cosmosNearestNode(
    model: CosmosModel,
    viewport: CosmosViewport,
    x: Float,
    y: Float,
    yaw: Float,
    pitch: Float,
): Int = model.nodes.indices.minBy { index ->
    val point = viewport.project(cosmosRotate(model.nodes[index], yaw, pitch))
    val dx = point.x - x
    val dy = point.y - y
    // Prefer the visible hemisphere when two projections nearly overlap.
    dx * dx + dy * dy + if (point.depth < 0f) viewport.radius * viewport.radius * 0.08f else 0f
}
