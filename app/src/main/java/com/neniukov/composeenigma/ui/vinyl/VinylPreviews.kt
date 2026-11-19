package com.neniukov.composeenigma.ui.vinyl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme

@Preview(name = "Vinyl playing", widthDp = 393, heightDp = 852, apiLevel = 36)
@Preview(name = "Vinyl compact", widthDp = 360, heightDp = 640, apiLevel = 36)
@Preview(name = "Vinyl wide", widthDp = 1_000, heightDp = 720, apiLevel = 36)
@Preview(name = "Vinyl large text", widthDp = 393, heightDp = 852, fontScale = 2f, apiLevel = 36)
@Composable
private fun VinylPlayingPreview() {
    VinylPreview(placement = 1f, arm = 1f)
}

@Preview(name = "Vinyl RTL", widthDp = 393, heightDp = 852, apiLevel = 36)
@Composable
private fun VinylRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        VinylPreview(placement = 1f, arm = 1f)
    }
}

@Preview(name = "Vinyl in sleeve", widthDp = 393, heightDp = 852, apiLevel = 36)
@Composable
private fun VinylSleevedPreview() {
    VinylPreview(placement = 0f, arm = 0f)
}

@Preview(name = "Vinyl lifting", widthDp = 393, heightDp = 852, apiLevel = 36)
@Composable
private fun VinylLiftingPreview() {
    VinylPreview(placement = 0.5f, arm = 0f)
}

@Composable
private fun VinylPreview(placement: Float, arm: Float) {
    val motion = remember(placement, arm) { VinylMotion(placement, arm) }
    MotionStudiesTheme {
        KineticVinylScreen(
            motion = motion,
            interactive = false,
            onBack = {},
            onTogglePlayback = {},
            onEject = {},
            onSelectSpeed = {},
        )
    }
}
