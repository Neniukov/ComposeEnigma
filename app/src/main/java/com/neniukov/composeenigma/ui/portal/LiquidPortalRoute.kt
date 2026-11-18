package com.neniukov.composeenigma.ui.portal

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

@Composable
internal fun LiquidPortalRoute(isActive: Boolean, onBack: () -> Unit) {
    var playing by remember { mutableStateOf(true) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val home = if (maxWidth > maxHeight) Offset(0.72f, 0.5f) else PortalMotion.Home
        val motion = rememberPortalMotion(isActive = isActive, playing = playing, home = home)
        LiquidPortalScreen(
            motion = motion,
            playing = playing,
            isActive = isActive,
            onBack = onBack,
            onTogglePlay = { playing = !playing },
            onTouch = { playing = false },
        )
    }
}
