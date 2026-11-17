package com.neniukov.composeenigma.ui.journey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme

data class MotionFrame(val progress: Float, val expanded: Boolean)

class MotionFrameProvider : PreviewParameterProvider<MotionFrame> {
    override val values: Sequence<MotionFrame>
        get() {
            val easing = JourneyMotion.Easing
            val opening = (0..35).asSequence().map { MotionFrame(easing.transform(it / 35f), true) }
            val closing = (0..35).asSequence().map { MotionFrame(1f - easing.transform(it / 35f), false) }
            return opening + closing
        }
}

@Preview(widthDp = 393, heightDp = 852, apiLevel = 36)
@Composable
fun MotionFramePreview(@PreviewParameter(MotionFrameProvider::class) frame: MotionFrame) {
    val progress = remember(frame) { mutableFloatStateOf(frame.progress) }
    MotionStudiesTheme {
        JourneyScreen(
            progress = progress,
            expanded = frame.expanded,
            saved = false,
            playing = false,
            onOpen = {},
            onClose = {},
            onSave = {},
            onPlay = {},
        )
    }
}
