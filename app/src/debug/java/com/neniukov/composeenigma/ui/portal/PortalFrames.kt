package com.neniukov.composeenigma.ui.portal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme

data class PortalFrame(
    val opening: Float,
    val center: Offset = Offset(0.5f, 0.55f),
    val drag: Offset = Offset.Zero,
    val time: Float = 0f,
    val ripple: Float = 1f,
)

class PortalFrameProvider : PreviewParameterProvider<PortalFrame> {
    override val values = sequenceOf(
        PortalFrame(opening = 0f),
        PortalFrame(opening = 0.25f, time = 0.15f),
        PortalFrame(opening = 1f, time = 2f),
        PortalFrame(opening = 1f, center = Offset(0.65f, 0.48f), drag = Offset(0.1f, -0.04f), time = 3.5f),
        PortalFrame(opening = 0.2f, time = 5f, ripple = 0.18f),
        PortalFrame(opening = 0f, time = 5.7f, ripple = 0.5f),
    )
}

@Preview(widthDp = 393, heightDp = 852, apiLevel = 36)
@Composable
fun PortalFramePreview(@PreviewParameter(PortalFrameProvider::class) frame: PortalFrame) {
    val motion = remember(frame) {
        PortalMotion(frame.opening, frame.center, frame.ripple).apply {
            time = frame.time
            target = frame.center + frame.drag
        }
    }
    MotionStudiesTheme {
        LiquidPortalScreen(motion, playing = false, isActive = false, onBack = {}, onTogglePlay = {}, onTouch = {})
    }
}
