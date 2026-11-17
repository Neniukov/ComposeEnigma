package com.neniukov.composeenigma.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TravelColors = lightColorScheme(
    primary = JourneyColors.Accent,
    onPrimary = JourneyColors.White,
    background = JourneyColors.Paper,
    onBackground = JourneyColors.Ink,
    surface = JourneyColors.Paper,
    onSurface = JourneyColors.Ink,
    secondary = JourneyColors.Muted,
)

@Composable
internal fun MotionStudiesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TravelColors, typography = Typography, content = content)
}
