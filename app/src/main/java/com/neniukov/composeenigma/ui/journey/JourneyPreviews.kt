package com.neniukov.composeenigma.ui.journey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme

@Preview(name = "01 Curated escape", widthDp = 393, heightDp = 852, apiLevel = 36)
@Composable
private fun CardPreview() = JourneyPreview(0f)

@Preview(name = "02 Container in flight", widthDp = 393, heightDp = 852, apiLevel = 36)
@Composable
private fun TransitionPreview() = JourneyPreview(.45f)

@Preview(name = "03 Into the mountains", widthDp = 393, heightDp = 852, apiLevel = 36)
@Composable
private fun DetailPreview() = JourneyPreview(1f)

@Preview(name = "04 Short window", widthDp = 360, heightDp = 640, apiLevel = 36)
@Preview(name = "05 Landscape", widthDp = 852, heightDp = 393, apiLevel = 36)
@Preview(name = "06 Large text", widthDp = 393, heightDp = 852, fontScale = 2f, apiLevel = 36)
@Composable
private fun AccessibleDetailPreview() = JourneyPreview(1f)

@Preview(name = "07 Short home", widthDp = 360, heightDp = 640, apiLevel = 36)
@Preview(name = "08 Large text home", widthDp = 393, heightDp = 852, fontScale = 2f, apiLevel = 36)
@Composable
private fun ScrollableHomePreview() = JourneyPreview(0f)

@Composable
private fun JourneyPreview(position: Float) {
    val progress = remember(position) { mutableFloatStateOf(position) }
    MotionStudiesTheme {
        JourneyScreen(
            progress = progress,
            expanded = position > 0f,
            saved = false,
            playing = false,
            onOpen = {},
            onClose = {},
            onSave = {},
            onPlay = {},
        )
    }
}
