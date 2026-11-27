package com.neniukov.composeenigma.ui.splice

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.neniukov.composeenigma.ui.theme.SpliceColors as C

internal class SpliceGesture {
    var offset by mutableStateOf(Offset.Zero)
    var anchor by mutableStateOf(SpliceLayout.band.center)
    var lift by mutableFloatStateOf(0f)

    fun reset() {
        offset = Offset.Zero
        anchor = SpliceLayout.band.center
        lift = 0f
    }
}

@Composable
internal fun Modifier.spliceKeyboardInput(enabled: Boolean, onJoin: () -> Unit): Modifier {
    if (!enabled) return this

    var focused by remember { mutableStateOf(false) }
    return this
        .onFocusChanged { focused = it.isFocused }
        .then(if (focused) Modifier.border(2.dp, C.Blue) else Modifier)
        .onKeyEvent { event ->
            when (event.key) {
                Key.Enter, Key.NumPadEnter, Key.Spacebar, Key.DirectionCenter -> {
                    if (event.type == KeyEventType.KeyUp) onJoin()
                    true
                }
                else -> false
            }
        }
        .focusable()
}

@Composable
internal fun Modifier.spliceInput(gesture: SpliceGesture, enabled: Boolean, onJoin: () -> Unit): Modifier {
    val join by rememberUpdatedState(onJoin)
    return pointerInput(gesture, enabled) {
        if (!enabled) return@pointerInput
        var committed = false
        try {
            coroutineScope {
                var returnJob: Job? = null
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (size.width == 0 || size.height == 0) return@awaitEachGesture
                    val scaleX = SpliceLayout.Width / size.width
                    val scaleY = SpliceLayout.PhotoHeight / size.height
                    val start = Offset(down.position.x * scaleX, down.position.y * scaleY)
                    val original = gesture.offset
                    if (!SpliceLayout.band.translate(original).contains(start)) return@awaitEachGesture

                    returnJob?.cancel()
                    gesture.anchor = start - original
                    gesture.lift = 1f
                    down.consume()
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) break
                        val position = Offset(change.position.x * scaleX, change.position.y * scaleY)
                        gesture.offset = original + position - start
                        change.consume()
                        if (!change.pressed) {
                            committed = SpliceLayout.acceptsDrop(gesture.offset, gesture.anchor)
                            if (committed) join()
                            break
                        }
                    }

                    if (!committed) {
                        val releaseOffset = gesture.offset
                        returnJob = launch {
                            animate(1f, 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) { value, _ ->
                                gesture.offset = releaseOffset * value
                                gesture.lift = value.coerceIn(0f, 1f)
                            }
                            gesture.reset()
                        }
                    }
                }
            }
        } finally {
            if (!committed) gesture.reset()
        }
    }
}
