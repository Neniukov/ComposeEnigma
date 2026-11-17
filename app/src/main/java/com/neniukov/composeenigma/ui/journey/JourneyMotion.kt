package com.neniukov.composeenigma.ui.journey

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal object JourneyMotion {
    const val DURATION_MILLIS = 1_150
    const val AUTOPLAY_LEAD_IN_MILLIS = 1_600L
    const val AUTOPLAY_EXPANDED_MILLIS = 3_800L
    const val AUTOPLAY_COLLAPSED_MILLIS = 3_000L

    val Easing = CubicBezierEasing(.22f, 1f, .36f, 1f)
    val HomeExit = MotionPhase(0f, .32f, (-28).dp)
    val BackReveal = MotionPhase(.32f, .8f, (-12).dp)
    val CoordinatesExit = MotionPhase(0f, .25f)
    val CaptionExit = MotionPhase(0f, .5f)
    val ArrowExit = MotionPhase(0f, .3f)
    val IntroductionReveal = MotionPhase(.25f, .65f, 22.dp)
    val StatsReveal = MotionPhase(.42f, .82f, 30.dp)
    val TrailReveal = MotionPhase(.53f, .91f, 35.dp)
    val SaveReveal = MotionPhase(.62f, 1f, 40.dp)

    const val HERO_ZOOM = .075f
    const val TITLE_SCALE = 1.22f
    const val ARROW_ROTATION = -45f
    val HeroCornerRadius = 26.dp
    val HeroLift = 12.dp
    val BookmarkDrop = 28.dp
    val TitleLift = 26.dp
    val TrailDashTravel = 70.dp
}

internal data class MotionPhase(val start: Float, val end: Float, val offsetY: Dp = 0.dp) {
    fun valueAt(progress: Float): Float {
        val fraction = ((progress - start) / (end - start)).coerceIn(0f, 1f)
        return fraction * fraction * (3f - 2f * fraction)
    }
}

internal fun mix(a: Float, b: Float, progress: Float) = a + (b - a) * progress

internal fun Modifier.morphBounds(
    progress: State<Float>,
    start: Rect,
    end: Rect,
    endOffsetY: () -> Int = { 0 },
) = layout { measurable, _ ->
    val p = progress.value
    val width = mix(start.width, end.width, p).roundToInt().coerceAtLeast(1)
    val height = mix(start.height, end.height, p).roundToInt().coerceAtLeast(1)
    val placeable = measurable.measure(Constraints.fixed(width, height))
    layout(width, height) {
        placeable.placeRelative(
            mix(start.left, end.left, p).roundToInt(),
            mix(start.top, end.top + endOffsetY(), p).roundToInt(),
        )
    }
}

internal fun Modifier.reveal(progress: State<Float>, phase: MotionPhase) =
    graphicsLayer {
        val visible = phase.valueAt(progress.value)
        alpha = visible
        translationY = phase.offsetY.toPx() * (1f - visible)
    }

internal class HeroShape(private val progress: State<Float>) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val corner = with(density) { JourneyMotion.HeroCornerRadius.toPx() } * (1f - progress.value)
        return Outline.Rounded(RoundRect(Rect(0f, 0f, size.width, size.height), CornerRadius(corner)))
    }
}
