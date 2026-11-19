package com.neniukov.composeenigma.ui.vinyl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme
import com.neniukov.composeenigma.ui.theme.VinylColors

class VinylDirectionProvider : PreviewParameterProvider<LayoutDirection> {
    override val values = sequenceOf(LayoutDirection.Ltr, LayoutDirection.Rtl)
}

@Preview(name = "Vinyl scene direction", widthDp = 393, heightDp = 393, apiLevel = 36)
@Composable
fun VinylSceneDirectionPreview(@PreviewParameter(VinylDirectionProvider::class) direction: LayoutDirection) {
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        val motion = remember { VinylMotion(initialPlacement = 1f, initialArm = 1f) }
        MotionStudiesTheme {
            Box(Modifier.fillMaxSize().background(VinylColors.Paper)) {
                VinylScene(motion = motion, interactive = false, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Preview(name = "Vinyl cover direction", widthDp = 393, heightDp = 262, apiLevel = 36)
@Composable
fun VinylCoverDirectionPreview(@PreviewParameter(VinylDirectionProvider::class) direction: LayoutDirection) {
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        MotionStudiesTheme {
            KineticVinylCover(Modifier.fillMaxSize())
        }
    }
}
