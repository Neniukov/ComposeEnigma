package com.neniukov.composeenigma.ui.cosmos

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import kotlin.math.sqrt
import com.neniukov.composeenigma.ui.theme.CosmosColors as C

@Composable
internal fun CosmosScene(motion: CosmosMotion, model: CosmosModel, modifier: Modifier = Modifier) {
    val renderer = remember(model) { CosmosRenderer(model) }
    Canvas(modifier.clipToBounds()) { with(renderer) { render(motion) } }
}

@Composable
internal fun CosmosCover(modifier: Modifier = Modifier) {
    val model = remember { CosmosModel() }
    val motion = remember { CosmosMotion(initialPhase = 0.7f, initialSignal = 0.27f) }
    val renderer = remember(model) { CosmosRenderer(model) }
    Canvas(modifier) {
        drawRect(C.Background)
        with(renderer) { render(motion) }
    }
}

/** Reused paths split at the camera plane, letting the core sit between the two hemispheres. */
private class CosmosStroke(val points: List<CosmosPoint>) {
    val back = Path()
    val front = Path()

    fun prepare(yaw: Float, pitch: Float, viewport: CosmosViewport) {
        back.rewind()
        front.rewind()
        var previous: CosmosProjection? = null
        points.forEach { original ->
            val point = viewport.project(cosmosRotate(original, yaw, pitch))
            previous?.let { start ->
                val path = if (start.depth + point.depth >= 0f) front else back
                path.moveTo(start.x, start.y)
                path.lineTo(point.x, point.y)
            }
            previous = point
        }
    }
}

private class CosmosRenderer(private val model: CosmosModel) {
    private val x = FloatArray(COSMOS_NODE_COUNT)
    private val y = FloatArray(COSMOS_NODE_COUNT)
    private val depth = FloatArray(COSMOS_NODE_COUNT)
    private val perspective = FloatArray(COSMOS_NODE_COUNT)
    private val energy = FloatArray(COSMOS_NODE_COUNT)
    private val distance = FloatArray(COSMOS_NODE_COUNT)
    private var lastSource = -1
    private val orbits = model.orbits.map(::CosmosStroke)
    private val filaments = model.filaments.map(::CosmosStroke)
    private val orbitColors = listOf(C.Blue, C.Cyan, C.Violet)

    fun DrawScope.render(motion: CosmosMotion) {
        val viewport = CosmosViewport(size.width, size.height)
        val radius = viewport.radius
        if (radius <= 0f) return
        val center = Offset(viewport.centerX, viewport.centerY)
        val phase = motion.phase
        val yaw = motion.yaw + phase
        val pitch = motion.pitch
        val signal = motion.signalProgress
        val source = model.nodes[motion.signalSource]
        if (lastSource != motion.signalSource) {
            model.nodes.forEachIndexed { index, point -> distance[index] = source.distanceTo(point) }
            lastSource = motion.signalSource
        }
        model.nodes.forEachIndexed { index, original ->
            val point = viewport.project(cosmosRotate(original, yaw, pitch))
            x[index] = point.x
            y[index] = point.y
            depth[index] = point.depth
            perspective[index] = point.perspective
            energy[index] = cosmosSignalEnergy(distance[index], signal)
        }
        orbits.forEach { it.prepare(yaw, pitch, viewport) }
        filaments.forEach { it.prepare(yaw, pitch, viewport) }

        glow(center + Offset(radius * 0.16f, -radius * 0.08f), radius * 1.65f, C.Blue, 0.13f)
        glow(center + Offset(-radius * 0.42f, radius * 0.32f), radius * 1.1f, C.Violet, 0.08f)
        drawStars(phase)
        drawOrbits(front = false, radius = radius)
        drawSatellites(phase, yaw, pitch, viewport, front = false)
        drawNetwork(front = false, radius = radius, phase = phase)
        drawFilaments(front = false, radius = radius)

        val breath = 0.90f + sin(phase * 7f) * 0.10f
        glow(center, radius * 0.52f, C.Blue, 0.08f * breath)
        glow(center, radius * 0.26f, C.Warm, 0.11f * breath)
        glow(center, radius * 0.10f, C.Core, 0.22f)
        drawCircle(C.Core.copy(alpha = 0.72f), radius * 0.013f, center)

        drawFilaments(front = true, radius = radius)
        drawNetwork(front = true, radius = radius, phase = phase)
        drawOrbits(front = true, radius = radius)
        drawSatellites(phase, yaw, pitch, viewport, front = true)
    }

    private fun DrawScope.drawStars(phase: Float) {
        repeat(74) { index ->
            val star = Offset(
                ((index * 137 + 31) % 997) / 997f * size.width,
                ((index * 263 + 79) % 991) / 991f * size.height,
            )
            val alpha = 0.13f + (sin(phase * 7f + index * 2.1f) + 1f) * 0.11f
            drawCircle(C.Ink.copy(alpha = alpha), if (index % 11 == 0) 1.6f else 0.8f, star)
            if (index % 19 == 0) {
                drawLine(C.Cyan.copy(alpha = alpha * 0.6f), star - Offset(4f, 0f), star + Offset(4f, 0f), 0.7f)
                drawLine(C.Cyan.copy(alpha = alpha * 0.6f), star - Offset(0f, 4f), star + Offset(0f, 4f), 0.7f)
            }
        }
    }

    private fun DrawScope.drawNetwork(front: Boolean, radius: Float, phase: Float) {
        val stroke = (radius * 0.002f).coerceAtLeast(0.55f)
        model.links.forEach { link ->
            if ((depth[link.from] + depth[link.to] >= 0f) != front) return@forEach
            val start = Offset(x[link.from], y[link.from])
            val end = Offset(x[link.to], y[link.to])
            val brightness = ((depth[link.from] + depth[link.to]) * 0.25f + 0.5f).coerceIn(0f, 1f)
            val rim = shellVisibility((start + end) * 0.5f, radius, (depth[link.from] + depth[link.to]) * 0.5f)
            val alpha = (0.12f + brightness * 0.33f) * (0.14f + rim * 0.86f)
            drawLine(C.Cyan.copy(alpha = alpha), start, end, stroke)
            val pulse = maxOf(energy[link.from], energy[link.to])
            if (pulse > 0.01f) {
                val signal = pulse * (0.55f + rim * 0.45f)
                drawLine(C.Cyan.copy(alpha = signal * 0.16f), start, end, stroke * 5f, StrokeCap.Round)
                drawLine(C.Core.copy(alpha = signal * 0.85f), start, end, stroke * 1.5f, StrokeCap.Round)
            }
        }
        model.nodes.indices.forEach { index ->
            if ((depth[index] >= 0f) != front) return@forEach
            val point = Offset(x[index], y[index])
            val pulse = energy[index]
            val visibility = 0.40f + shellVisibility(point, radius, depth[index]) * 0.60f
            val accent = if (index % 29 == 0) {
                C.Warm
            } else if (index % 3 == 0) {
                C.Blue
            } else {
                C.Cyan
            }
            val shimmer = (sin(phase * 5f + index * 1.7f) + 1f) * 0.5f
            val alpha = (if (front) 0.68f + shimmer * 0.30f else 0.24f + shimmer * 0.14f) * visibility
            val dot = radius * (if (index % 13 == 0) 0.009f else 0.005f) * perspective[index]
            if ((front && index % 4 == 0) || pulse > 0.05f) {
                drawCircle(accent.copy(alpha = 0.05f * visibility + pulse * 0.10f), dot * (5f + pulse * 5f), point)
                drawCircle(accent.copy(alpha = 0.15f * visibility + pulse * 0.17f), dot * (2f + pulse * 2f), point)
            }
            drawCircle(accent.copy(alpha = alpha), dot + pulse * radius * 0.008f, point)
            if (pulse > 0.10f) drawCircle(C.Core.copy(alpha = pulse), dot * 0.8f, point)
        }
    }

    private fun DrawScope.drawFilaments(front: Boolean, radius: Float) {
        filaments.forEach { filament ->
            val path = if (front) filament.front else filament.back
            val color = C.Warm
            val alpha = if (front) 0.30f else 0.065f
            drawPath(path, color.copy(alpha = alpha * 0.08f), style = Stroke(radius * 0.028f))
            drawPath(path, color.copy(alpha = alpha * 0.20f), style = Stroke(radius * 0.009f))
            drawPath(path, color.copy(alpha = alpha), style = Stroke((radius * 0.0025f).coerceAtLeast(0.6f)))
        }
    }

    /** Only the rear hemisphere fades toward the core, blending continuously at the camera plane. */
    private fun DrawScope.shellVisibility(point: Offset, radius: Float, depth: Float): Float {
        val offset = point - center
        val distance = sqrt(offset.x * offset.x + offset.y * offset.y) / radius
        val progress = ((distance - 0.55f) / 0.40f).coerceIn(0f, 1f)
        val rim = progress * progress * (3f - 2f * progress)
        val rearDepth = (-depth).coerceIn(0f, 1f)
        return 1f - (1f - rim) * rearDepth
    }

    private fun DrawScope.drawOrbits(front: Boolean, radius: Float) {
        orbits.forEachIndexed { index, orbit ->
            val path = if (front) orbit.front else orbit.back
            val color = orbitColors[index]
            val alpha = if (front) 0.42f else 0.12f
            drawPath(path, color.copy(alpha = alpha * 0.12f), style = Stroke(radius * 0.014f))
            drawPath(path, color.copy(alpha = alpha), style = Stroke((radius * 0.0025f).coerceAtLeast(0.65f)))
        }
    }

    private fun DrawScope.drawSatellites(
        phase: Float,
        yaw: Float,
        pitch: Float,
        viewport: CosmosViewport,
        front: Boolean,
    ) {
        repeat(3) { index ->
            val angle = phase * (index + 2) + index * 2.1f
            val point = viewport.project(cosmosRotate(cosmosOrbitPoint(angle, index), yaw, pitch))
            if ((point.depth >= 0f) != front) return@repeat
            val center = Offset(point.x, point.y)
            val radius = viewport.radius * 0.012f * point.perspective
            val color = if (index == 1) C.Warm else C.Cyan
            glow(center, radius * 6f, color, if (front) 0.45f else 0.15f)
            drawCircle(color, radius, center)
            drawCircle(C.Core, radius * 0.45f, center)
        }
    }

    private fun DrawScope.glow(center: Offset, radius: Float, color: Color, alpha: Float) {
        drawCircle(
            Brush.radialGradient(listOf(color.copy(alpha = alpha), Color.Transparent), center, radius),
            radius,
            center,
        )
    }
}
