package com.neniukov.composeenigma.ui.signal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.repeatOnLifecycle
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.atan2
import com.neniukov.composeenigma.ui.theme.SignalColors as C

@Composable
internal fun SignalLockRoute(isActive: Boolean, onBack: () -> Unit) {
    val session = rememberSaveable(saver = SignalSession.Saver) { SignalSession() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateAsState()
    val enabled = isActive && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    LaunchedEffect(lifecycle, isActive, session) {
        if (!isActive) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val durationScale = currentCoroutineContext()[MotionDurationScale]
            snapshotFlow { durationScale?.scaleFactor ?: 1f }.collectLatest { scale ->
                session.reduceMotion(scale == 0f)
                if (scale > 0f) {
                    var previous = withFrameNanos { it }
                    while (true) {
                        val now = withFrameNanos { it }
                        session.advance((now - previous) / 1_000_000_000f / scale)
                        previous = now
                    }
                }
            }
        }
    }

    SignalLockScreen(session, enabled, onBack)
}

@Composable
private fun SignalLockScreen(session: SignalSession, enabled: Boolean, onBack: () -> Unit) {
    val background = remember {
        Brush.verticalGradient(listOf(C.Background, C.Glow, C.Background))
    }
    BoxWithConstraints(
        Modifier.fillMaxSize().background(background).windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val wide = maxWidth > maxHeight && maxWidth >= 570.dp
        val contentWidth = if (wide) 1000.dp else 470.dp
        val horizontalPadding = if (wide) 30.dp else 20.dp

        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            SignalToolbar(
                session = session,
                enabled = enabled,
                onBack = onBack,
                modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 8.dp, bottom = 8.dp),
            )
            BoxWithConstraints(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (wide) {
                    val cabinetWidth = minOf(maxWidth * .53f, maxHeight * .83f, 460.dp)
                    Row(
                        Modifier.widthIn(max = contentWidth).fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            SignalTitle()
                            SignalStatus(session)
                        }
                        SignalStage(session, enabled, Modifier.width(cabinetWidth))
                    }
                } else {
                    Column(
                        Modifier.widthIn(max = contentWidth).fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        SignalTitle(Modifier.fillMaxWidth())
                        SignalStage(session, enabled, Modifier.fillMaxWidth())
                        SignalStatus(session, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalToolbar(
    session: SignalSession,
    enabled: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        SignalButton(stringResource(R.string.signal_back), enabled, onBack, isBack = true)
        SignalButton(stringResource(R.string.signal_reset), enabled, session::reset)
    }
}

@Composable
private fun SignalButton(label: String, enabled: Boolean, onClick: () -> Unit, isBack: Boolean = false) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier.heightIn(min = 48.dp).background(C.Surface, shape).border(1.dp, C.Edge, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (isBack) GlyphView(Glyph.Back, C.Muted, Modifier.size(17.dp))
        Text(label, color = C.Ink, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}

@Composable
private fun SignalTitle(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            stringResource(R.string.signal_title),
            Modifier.semantics { heading() },
            color = C.Ink,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 43.sp,
            lineHeight = 46.sp,
            letterSpacing = (-1.7f).sp,
        )
        Text(
            stringResource(R.string.signal_hint),
            color = C.Muted,
            fontSize = 14.sp,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun SignalStatus(session: SignalSession, modifier: Modifier = Modifier) {
    val locked = session.locked
    val status = stringResource(if (locked) R.string.signal_locked else R.string.signal_searching)
    Row(
        modifier.border(1.dp, C.Edge, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val color = if (locked) C.Mint else C.Amber
        Canvas(Modifier.size(9.dp)) {
            drawCircle(color.copy(alpha = 0.28f))
            drawCircle(color, radius = size.minDimension * 0.28f)
        }
        Text(
            status,
            Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.6.sp,
        )
    }
}

@Composable
private fun SignalStage(session: SignalSession, enabled: Boolean, modifier: Modifier = Modifier) {
    val artwork = rememberSignalArtwork()
    val dialDescription = stringResource(R.string.signal_dial_description)
    var dialFocused by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier.aspectRatio(SignalCabinet.Width / SignalCabinet.Height)) {
        Canvas(Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.withScale(size.width / SignalCabinet.Width, size.height / SignalCabinet.Height) {
                    artwork.draw(this, session.frame, dialFocused)
                }
            }
        }

        val unit = maxWidth / SignalCabinet.Width
        val radius = SignalCabinet.DialHitRadius
        Box(
            Modifier.absoluteOffset(
                x = unit * (SignalCabinet.DialX - radius),
                y = unit * (SignalCabinet.DialY - radius),
            )
                .size(unit * (radius * 2f))
                .semantics {
                    contentDescription = dialDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(session.tuning, 0f..1f)
                    if (enabled) setProgress { session.seek(it) } else disabled()
                }
                .onFocusChanged { dialFocused = it.isFocused }
                .onKeyEvent {
                    val delta = when (it.key) {
                        Key.DirectionLeft, Key.DirectionDown -> -0.0375f
                        Key.DirectionRight, Key.DirectionUp -> 0.0375f
                        else -> return@onKeyEvent false
                    }
                    if (!enabled) return@onKeyEvent false
                    if (it.type == KeyEventType.KeyDown) session.seek(session.tuning + delta)
                    true
                }
                .focusable(enabled)
                .turnDial(session, enabled),
        )
    }
}

private fun Modifier.turnDial(session: SignalSession, enabled: Boolean): Modifier =
    pointerInput(session, enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown()
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            if ((down.position - center).getDistance() > center.x) return@awaitEachGesture

            val horizontal = (down.position - center).getDistance() < center.x * .40f
            var previous = down.position
            var previousTime = down.uptimeMillis
            var lastMovement = previousTime
            var completed = false
            down.consume()
            session.grab()
            try {
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    if (change.isConsumed) break
                    val moved = change.position != previous
                    if (moved) {
                        val delta = if (horizontal) {
                            (change.position.x - previous.x) / size.width
                        } else {
                            shortestDegrees((previous - center).degrees(), (change.position - center).degrees()) / DialSweep
                        }
                        session.turn(delta, (change.uptimeMillis - previousTime) / 1000f)
                        lastMovement = change.uptimeMillis
                    }
                    previous = change.position
                    previousTime = change.uptimeMillis
                    change.consume()
                    if (!change.pressed) {
                        completed = true
                        session.release(canceled = false, allowMomentum = change.uptimeMillis - lastMovement <= 110)
                        break
                    }
                }
            } finally {
                if (!completed) session.release(canceled = true)
            }
        }
    }

private fun Offset.degrees(): Float = Math.toDegrees(atan2(x, -y).toDouble()).toFloat()

@Composable
private fun rememberSignalArtwork(): SignalArtwork {
    val frequency = stringResource(R.string.signal_frequency)
    val unit = stringResource(R.string.signal_unit)
    val tuning = stringResource(R.string.signal_tuning)
    val sync = stringResource(R.string.signal_sync)
    return remember(frequency, unit, tuning, sync) { SignalArtwork(frequency, unit, tuning, sync) }
}

@Composable
internal fun SignalLockCover(modifier: Modifier = Modifier) {
    val artwork = rememberSignalArtwork()
    val frame = remember { SignalTuner(CarrierTuning).apply { reduceMotion(true) }.frame }
    Canvas(modifier.background(C.Background)) {
        drawIntoCanvas { canvas ->
            val scale = minOf(size.width / SignalCabinet.Width, size.height / SignalCabinet.Height)
            canvas.nativeCanvas.withTranslation(
                (size.width - SignalCabinet.Width * scale) / 2f,
                (size.height - SignalCabinet.Height * scale) / 2f,
            ) {
                withScale(scale, scale) { artwork.draw(this, frame) }
            }
        }
    }
}

private class SignalSession(initialTuning: Float = InitialTuning) {
    private val tuner = SignalTuner(initialTuning)

    var frame by mutableStateOf(tuner.frame)
        private set
    var tuning by mutableFloatStateOf(frame.tuning)
        private set
    var locked by mutableStateOf(frame.locked)
        private set

    fun advance(seconds: Float) = publish(tuner.step(seconds))

    fun grab() = tuner.grab()

    fun turn(delta: Float, seconds: Float) {
        tuner.turn(delta, seconds)
        publish(tuner.frame)
    }

    fun release(canceled: Boolean, allowMomentum: Boolean = false) {
        tuner.release(canceled, allowMomentum)
        publish(tuner.frame)
    }

    fun seek(value: Float): Boolean {
        if (!value.isFinite() || tuning == value.coerceIn(0f, 1f)) return false
        tuner.seek(value)
        publish(tuner.frame)
        return true
    }

    fun reduceMotion(reduce: Boolean) {
        tuner.reduceMotion(reduce)
        publish(tuner.frame)
    }

    fun reset() {
        tuner.reset()
        publish(tuner.frame)
    }

    private fun publish(next: SignalFrame) {
        frame = next
        tuning = next.tuning
        locked = next.locked
    }

    companion object {
        val Saver = Saver<SignalSession, Float>(save = { it.tuning }, restore = { SignalSession(it) })
    }
}
