package com.neniukov.composeenigma.ui.portal

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme

@Preview(name = "Liquid Portal", widthDp = 393, heightDp = 852, apiLevel = 36)
@Preview(name = "Liquid Portal compact", widthDp = 360, heightDp = 640, apiLevel = 36)
@Preview(name = "Liquid Portal landscape", widthDp = 852, heightDp = 393, apiLevel = 36)
@Preview(name = "Liquid Portal Canvas", widthDp = 393, heightDp = 852, apiLevel = 32)
@Preview(name = "Liquid Portal large text", widthDp = 393, heightDp = 852, fontScale = 2f, apiLevel = 36)
@Composable
private fun LiquidPortalPreview() {
    MotionStudiesTheme {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val home = if (maxWidth > maxHeight) Offset(0.72f, 0.5f) else PortalMotion.Home
            val motion = remember(home) { PortalMotion(initialOpening = 1f, initialCenter = home) }
            LiquidPortalScreen(
                motion,
                playing = false,
                isActive = false,
                onBack = {},
                onTogglePlay = {},
                onTouch = {},
            )
        }
    }
}
