package com.neniukov.composeenigma.ui.droid

import androidx.compose.ui.graphics.Color
import com.neniukov.composeenigma.ui.theme.DroidColors as C
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

internal data class DroidVector(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: DroidVector) = DroidVector(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: DroidVector) = DroidVector(x - other.x, y - other.y, z - other.z)
    operator fun times(factor: Float) = DroidVector(x * factor, y * factor, z * factor)
    fun dot(other: DroidVector) = x * other.x + y * other.y + z * other.z
    fun cross(other: DroidVector) = DroidVector(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
    fun normalized(): DroidVector {
        val length = sqrt(dot(this)).coerceAtLeast(0.00001f)
        return this * (1f / length)
    }
}

internal class DroidRotation(x: Float, y: Float, z: Float) {
    private val cx = cos(x)
    private val sx = sin(x)
    private val cy = cos(y)
    private val sy = sin(y)
    private val cz = cos(z)
    private val sz = sin(z)

    fun apply(p: DroidVector): DroidVector {
        val a = p.y * cx - p.z * sx
        val b = p.y * sx + p.z * cx
        val c = p.x * cy + b * sy
        return DroidVector(c * cz - a * sz, c * sz + a * cz, -p.x * sy + b * cy)
    }
}

internal enum class DroidJoint { Body, Head, LeftArm, RightArm, LeftLeg, RightLeg }

internal class DroidFace(
    val vertices: List<DroidVector>,
    val normal: DroidVector,
    val color: Color,
    val luminous: Boolean = false,
    val vertexNormals: List<DroidVector> = vertices.map { normal },
)

internal class DroidPart(
    val joint: DroidJoint,
    val origin: DroidVector,
    val faces: List<DroidFace>,
    val eye: Boolean = false,
)

/** Geometry is built once, including the curved eyes on the surface of the dome. */
internal class DroidModel {
    val parts: List<DroidPart> = buildList {
        add(DroidPart(DroidJoint.Body, DroidVector(0f, -0.12f, 0f), lathe(0.76f, 0.65f, 0.55f, C.Green, 10, 0.22f)))
        add(DroidPart(DroidJoint.Head, DroidVector(0f, 0.63f, 0f), dome()))
        for (side in listOf(-1f, 1f)) {
            val armJoint = if (side < 0f) DroidJoint.LeftArm else DroidJoint.RightArm
            val legJoint = if (side < 0f) DroidJoint.LeftLeg else DroidJoint.RightLeg
            add(DroidPart(armJoint, DroidVector(side * 1.02f, 0.24f, 0f),
                lathe(0.19f, 0.54f, 0.19f, C.Green, 10, 0.58f).shift(DroidVector(0f, -0.4f, 0f))))
            add(DroidPart(legJoint, DroidVector(side * 0.36f, -0.65f, 0f),
                lathe(0.21f, 0.43f, 0.24f, C.Green, 8, 0.45f).shift(DroidVector(0f, -0.43f, 0f))))
            val antennaRotation = DroidRotation(0f, 0f, -side * 0.48f)
            val antenna = lathe(0.037f, 0.3f, 0.037f, C.Green, 6, 0.7f, 10)
                .map { face -> DroidFace(face.vertices.map { antennaRotation.apply(it) + DroidVector(side * 0.54f, 0.91f, 0f) }, antennaRotation.apply(face.normal), face.color, vertexNormals = face.vertexNormals.map(antennaRotation::apply)) }
            add(DroidPart(DroidJoint.Head, DroidVector(0f, 0.63f, 0f), antenna))
            add(DroidPart(DroidJoint.Head, DroidVector(0f, 0.63f, 0f), eye(side * 0.32f), eye = true))
        }
    }

    private fun dome(): List<DroidFace> = surface(32, 10, C.Green) { u, v ->
        val angle = u * 2f * PI.toFloat()
        val elevation = v * PI.toFloat() / 2f
        DroidVector(cos(elevation) * sin(angle) * 0.9f, sin(elevation) * 0.79f, cos(elevation) * cos(angle) * 0.7f)
    } + surface(32, 1, C.DarkGreen) { u, v ->
        val a = u * 2f * PI.toFloat()
        DroidVector(sin(a) * 0.9f * v, 0f, cos(a) * 0.7f * v)
    }

    private fun eye(centerX: Float): List<DroidFace> = surface(20, 2, C.Eye, luminous = true) { u, v ->
        val angle = u * 2f * PI.toFloat()
        val x = centerX + cos(angle) * 0.071f * v
        val y = 0.31f + sin(angle) * 0.079f * v
        val z = 0.7f * sqrt((1f - (x / 0.9f).pow(2) - (y / 0.79f).pow(2)).coerceAtLeast(0f)) + 0.009f
        DroidVector(x, y, z)
    }

    private fun lathe(rx: Float, ry: Float, rz: Float, color: Color, rings: Int, exponent: Float, segments: Int = 24) =
        surface(segments, rings, color) { u, v ->
            val angle = u * 2f * PI.toFloat()
            val lat = (v - 0.5f) * PI.toFloat()
            val radius = if (v == 0f || v == 1f) 0f else abs(cos(lat)).pow(exponent)
            val y = sin(lat).let { if (it < 0f) -abs(it).pow(exponent) else it.pow(exponent) }
            DroidVector(sin(angle) * rx * radius, y * ry, cos(angle) * rz * radius)
        }

    private fun surface(
        segments: Int,
        rings: Int,
        color: Color,
        luminous: Boolean = false,
        point: (Float, Float) -> DroidVector,
    ): List<DroidFace> {
        val faces = buildList {
            for (j in 0 until rings) for (i in 0 until segments) {
                val a = point(i.toFloat() / segments, j.toFloat() / rings)
                val b = point((i + 1f) / segments, j.toFloat() / rings)
                val c = point((i + 1f) / segments, (j + 1f) / rings)
                val d = point(i.toFloat() / segments, (j + 1f) / rings)
                val center = (a + b + c + d) * 0.25f
                var normal = (c - a).cross(d - b).normalized()
                if (normal.dot(center) < 0f) normal = normal * -1f
                if (normal.dot(normal) > 0.1f) {
                    add(DroidFace(listOf(a, b, c, d), normal, color, luminous))
                }
            }
        }
        fun key(p: DroidVector) = Triple(
            (p.x * 10000f).roundToInt(),
            (p.y * 10000f).roundToInt(),
            (p.z * 10000f).roundToInt(),
        )
        val sums = HashMap<Triple<Int, Int, Int>, DroidVector>()
        faces.forEach { face ->
            face.vertices.forEach { p ->
                val key = key(p)
                sums[key] = (sums[key] ?: DroidVector(0f, 0f, 0f)) + face.normal
            }
        }
        return faces.map { face ->
            DroidFace(
                vertices = face.vertices,
                normal = face.normal,
                color = color,
                luminous = luminous,
                vertexNormals = face.vertices.map { sums.getValue(key(it)).normalized() },
            )
        }
    }
}

private fun List<DroidFace>.shift(offset: DroidVector) = map { face ->
    DroidFace(face.vertices.map { it + offset }, face.normal, face.color, face.luminous, face.vertexNormals)
}
