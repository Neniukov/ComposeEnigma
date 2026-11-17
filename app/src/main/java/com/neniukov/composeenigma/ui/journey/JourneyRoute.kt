package com.neniukov.composeenigma.ui.journey

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun JourneyRoute(saved: Boolean, onSave: () -> Unit, isActive: Boolean) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateAsState()
    val transition = updateTransition(expanded, label = "escape container")
    val progress = transition.animateFloat(
        transitionSpec = {
            tween(JourneyMotion.DURATION_MILLIS, easing = JourneyMotion.Easing)
        },
        label = "continuous hero bounds",
    ) { if (it) 1f else 0f }

    LaunchedEffect(playing, isActive, lifecycle) {
        if (playing && isActive) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                expanded = false
                delay(JourneyMotion.AUTOPLAY_LEAD_IN_MILLIS.milliseconds)
                while (true) {
                    expanded = true
                    delay(JourneyMotion.AUTOPLAY_EXPANDED_MILLIS.milliseconds)
                    expanded = false
                    delay(JourneyMotion.AUTOPLAY_COLLAPSED_MILLIS.milliseconds)
                }
            }
        }
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = isActive && lifecycleState.isAtLeast(Lifecycle.State.RESUMED) && (expanded || playing),
        onBackCompleted = {
            playing = false
            expanded = false
        },
    )
    JourneyScreen(
        progress = progress,
        expanded = expanded,
        saved = saved,
        playing = playing,
        onOpen = {
            playing = false
            expanded = true
        },
        onClose = {
            playing = false
            expanded = false
        },
        onSave = onSave,
        onPlay = { playing = !playing },
        transitionRunning = transition.isRunning,
    )
}
