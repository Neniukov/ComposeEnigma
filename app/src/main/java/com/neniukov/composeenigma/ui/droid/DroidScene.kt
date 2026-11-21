package com.neniukov.composeenigma.ui.droid

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.VertexMode
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import com.neniukov.composeenigma.ui.theme.DroidColors as C
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun DroidScene(motion: DroidMotion, modifier: Modifier = Modifier) {
    val renderer = remember { DroidRenderer() }
    Canvas(modifier) { with(renderer) { render(motion) } }
}

@Composable
internal fun DroidCover(modifier: Modifier = Modifier) {
    val renderer = remember { DroidRenderer() }
    val motion = remember { DroidMotion() }
    Canvas(modifier) {
        drawRect(C.Background)
        with(renderer) { render(motion, cover = true) }
    }
}

private class ProjectedFace(val face: DroidFace) {
    val points = FloatArray(8)
    var depth = 0f
    val colors = IntArray(4)
    val red = face.color.red
    val green = face.color.green
    val blue = face.color.blue
    val unlit = face.color.toArgb()
}

/** Small painter's-algorithm renderer: perspective, backface culling and world-space lighting. */
private class DroidRenderer {
    private val model = DroidModel()
    private val buffers = model.parts.map { part -> part.faces.map(::ProjectedFace) }
    private val visible = ArrayList<ProjectedFace>(1200)
    private val faceCount = model.parts.sumOf { it.faces.size }
    private val mesh = Vertices(
        VertexMode.Triangles,
        positions = List(faceCount * 4) { Offset.Zero },
        textureCoordinates = List(faceCount * 4) { Offset.Zero },
        colors = List(faceCount * 4) { Color.White },
        indices = List(faceCount * 6) { i -> (i / 6) * 4 + intArrayOf(0, 1, 2, 0, 2, 3)[i % 6] },
    )
    private val paint = Paint().apply { color = Color.White; isAntiAlias = true }
    private val light = DroidVector(-0.55f, 0.8f, 1f).normalized()
    private val highlight = (light + DroidVector(0f, 0f, 1f)).normalized()
    private val shadowRed = C.DarkGreen.red
    private val shadowGreen = C.DarkGreen.green
    private val shadowBlue = C.DarkGreen.blue

    fun DrawScope.render(motion: DroidMotion, cover: Boolean = false) {
        val t = motion.time
        val lift = motion.lift
        val scale = min(size.width / if (cover) 4.9f else 4.7f, size.height / 4.8f)
        val center = Offset(size.width * 0.5f, size.height * if (cover) 0.48f else 0.45f)
        val hover = sin(t * 1.8f) * 0.085f
        val groundY = center.y + scale * 1.85f

        // A broad soft pool of light, with a tight contact shadow underneath the floating robot.
        drawOval(C.Stage.copy(alpha = 0.5f), Offset(center.x - scale * 1.75f, groundY - scale * 0.32f), Size(scale * 3.5f, scale * 0.64f))
        for (ring in 0..2) {
            val width = scale * (2.7f + ring * 0.63f)
            drawOval(C.Line.copy(alpha = 0.6f - ring * 0.14f), Offset(center.x - width / 2, groundY - width * 0.095f), Size(width, width * 0.19f), style = Stroke(1f))
        }
        val shadowWidth = scale * (2.2f - lift * 0.5f)
        for (i in 14 downTo 1) {
            val radius = shadowWidth * i / 28f
            drawOval(C.Ink.copy(alpha = (0.012f - lift * 0.006f).coerceAtLeast(0.002f)),
                Offset(center.x - radius, groundY - radius * 0.18f), Size(radius * 2f, radius * 0.36f))
        }

        val global = DroidRotation(motion.pitch + motion.flip, motion.yaw, sin(t * 1.3f) * 0.025f)
        val head = DroidRotation(sin(t * 1.1f) * 0.025f, sin(t * 0.9f) * 0.09f, sin(t * 1.4f) * 0.045f)
        val wave = motion.wave
        val blinkPhase = t % 5.3f
        val blink = if (blinkPhase in 4.7f..4.9f) (1f - sin((blinkPhase - 4.7f) / 0.2f * PI.toFloat()) * 0.94f) else 1f
        visible.clear()
        model.parts.forEachIndexed { index, part ->
            val rotation = when (part.joint) {
                DroidJoint.Head -> head
                DroidJoint.LeftArm -> DroidRotation(0.04f, 0f, -0.12f - sin(t * 1.8f) * 0.04f)
                DroidJoint.RightArm -> DroidRotation(0f, 0f, 0.12f + wave * (2.25f + sin(motion.trickProgress * 24f) * 0.28f))
                DroidJoint.LeftLeg -> DroidRotation(sin(t * 1.8f) * 0.06f, 0f, -0.025f)
                DroidJoint.RightLeg -> DroidRotation(-sin(t * 1.8f) * 0.06f, 0f, 0.025f)
                DroidJoint.Body -> DroidRotation(0f, 0f, 0f)
            }
            buffers[index].forEach { projected ->
                val face = projected.face
                val normal = global.apply(rotation.apply(face.normal))
                var depth = 0f
                var worldCenter = DroidVector(0f, 0f, 0f)
                face.vertices.forEachIndexed { vertexIndex, original ->
                    val vertex = if (part.eye) original.copy(y = 0.31f + (original.y - 0.31f) * blink) else original
                    val p = global.apply(rotation.apply(vertex) + part.origin)
                    worldCenter += p * 0.25f
                    val perspective = 7.5f / (7.5f - p.z)
                    projected.points[vertexIndex * 2] = center.x + p.x * scale * perspective
                    projected.points[vertexIndex * 2 + 1] = center.y - (p.y + hover + lift) * scale * perspective
                    depth += p.z
                }
                if (normal.dot(DroidVector(-worldCenter.x, -worldCenter.y, 7.5f - worldCenter.z)) > 0f) {
                    // The eyes sit on the dome; a small depth bias avoids coplanar sorting artifacts.
                    projected.depth = depth + if (part.eye) 0.36f else 0f
                    face.vertexNormals.forEachIndexed { i, vertexNormal ->
                        projected.colors[i] = if (face.luminous) projected.unlit else shade(projected, global.apply(rotation.apply(vertexNormal)))
                    }
                    visible.add(projected)
                }
            }
        }
        visible.sortBy { it.depth }
        visible.forEachIndexed { i, projected ->
            projected.points.copyInto(mesh.positions, i * 8)
            projected.colors.copyInto(mesh.colors, i * 4)
        }
        // Unused triangles are degenerate. The cached buffers keep all geometry in one draw call.
        mesh.positions.fill(0f, visible.size * 8)
        drawContext.canvas.drawVertices(mesh, BlendMode.Modulate, paint)
        if (!cover) {
            val orb = Offset(center.x + scale * 1.52f, center.y - scale * 0.9f)
            drawCircle(Brush.radialGradient(listOf(C.Green.copy(alpha = 0.2f), Color.Transparent), orb, scale * 0.3f), scale * 0.3f, orb)
            drawCircle(C.Green, scale * 0.045f, orb)
            drawCircle(C.DarkGreen.copy(alpha = 0.2f), scale * 0.095f, orb, style = Stroke(1f))
        }
    }

    private fun shade(face: ProjectedFace, normal: DroidVector): Int {
        val diffuse = 0.25f + normal.dot(light).coerceAtLeast(0f) * 0.75f
        val spec = normal.dot(highlight).coerceAtLeast(0f)
        val s2 = spec * spec
        val s4 = s2 * s2
        val s8 = s4 * s4
        val s16 = s8 * s8
        val shine = s16 * s8 * s4 * s2 * 0.7f
        // Lighting mixes RGB channels directly; perceptual Color.lerp converts color spaces.
        fun channel(shadow: Float, base: Float): Int {
            val lit = shadow + (base - shadow) * diffuse
            return ((lit + (1f - lit) * shine) * 255f).toInt().coerceIn(0, 255)
        }
        return (0xFF shl 24) or (channel(shadowRed, face.red) shl 16) or
            (channel(shadowGreen, face.green) shl 8) or channel(shadowBlue, face.blue)
    }
}
