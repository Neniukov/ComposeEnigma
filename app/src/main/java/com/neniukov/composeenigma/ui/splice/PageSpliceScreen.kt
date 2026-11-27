package com.neniukov.composeenigma.ui.splice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.repeatOnLifecycle
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.collectLatest
import com.neniukov.composeenigma.ui.theme.SpliceColors as C

@Composable
internal fun PageSpliceRoute(isActive: Boolean, onBack: () -> Unit) {
    val room = ImageBitmap.imageResource(R.drawable.splice_room)
    val yard = ImageBitmap.imageResource(R.drawable.splice_yard)
    val artwork = remember(room, yard) { PageSpliceArtwork(room, yard) }
    var joined by rememberSaveable { mutableStateOf(false) }
    var replay by remember { mutableIntStateOf(0) }

    key(replay) {
        PageSpliceScreen(
            artwork = artwork,
            isActive = isActive,
            joined = joined,
            onJoin = { joined = true },
            onReplay = {
                joined = false
                replay++
            },
            onBack = onBack,
        )
    }
}

@Composable
private fun PageSpliceScreen(
    artwork: PageSpliceArtwork,
    isActive: Boolean,
    joined: Boolean,
    onJoin: () -> Unit,
    onReplay: () -> Unit,
    onBack: () -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateAsState()
    val canInteract = isActive && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val download = remember { Animatable(if (joined) 1f else 0f) }
    val snap = remember { Animatable(if (joined) 1f else 0f) }
    val reveal = remember { Animatable(if (joined) 1f else 0f) }
    val isReady by remember { derivedStateOf { download.value == 1f } }
    val finished by remember { derivedStateOf { reveal.value == 1f } }
    val canJoin = canInteract && isReady && !joined
    val gesture = remember { SpliceGesture() }
    val joinWithoutDrag = {
        gesture.reset()
        onJoin()
    }
    var seconds by remember { mutableFloatStateOf(0f) }
    var photosInRoot by remember { mutableStateOf(Rect.Zero) }

    LaunchedEffect(isActive, joined, lifecycle) {
        if (!isActive) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (joined) {
                snap.animateTo(1f, tween(290))
                reveal.animateTo(1f, tween(1120, easing = FastOutSlowInEasing))
            } else if (!isReady) {
                download.animateTo(1f, tween(((1f - download.value) * 1780).toInt()))
            }
        }
    }
    LaunchedEffect(isActive, joined, lifecycle) {
        if (!isActive || !joined) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val durationScale = currentCoroutineContext()[MotionDurationScale]
            snapshotFlow { durationScale?.scaleFactor ?: 1f }.collectLatest { scale ->
                if (scale == 0f) {
                    // Hold a fully revealed frame, including the settled snowfall.
                    seconds = maxOf(seconds, 0.6f)
                } else {
                    var previousFrame = withFrameNanos { it }
                    while (true) {
                        val frame = withFrameNanos { it }
                        seconds += ((frame - previousFrame) / 1_000_000_000f)
                            .coerceAtMost(0.05f) / scale
                        previousFrame = frame
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize().background(C.Night)) {
        if (!finished) {
            DesktopWallpaper(Modifier.fillMaxSize())
            Box(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 18.dp, vertical = 58.dp),
                contentAlignment = Alignment.Center,
            ) {
                PageSpliceBrowser(
                    isReady = isReady,
                    progress = { download.value },
                    modifier = Modifier.widthIn(max = 496.dp).fillMaxWidth()
                        .graphicsLayer {
                            alpha = 1f - reveal.value
                            translationY = size.height * reveal.value * 0.12f
                        }
                        .verticalScroll(rememberScrollState()),
                ) {
                    val sceneDescription = stringResource(R.string.splice_scene_description)
                    val joinAction = stringResource(R.string.splice_join_action)
                    Canvas(
                        Modifier.fillMaxWidth()
                            .aspectRatio(SpliceLayout.Width / SpliceLayout.PhotoHeight)
                            .onGloballyPositioned { coordinates ->
                                if (!joined) {
                                    val origin = coordinates.positionInRoot()
                                    val bounds = Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
                                    photosInRoot = Rect(origin, bounds)
                                }
                            }
                            .semantics {
                                contentDescription = sceneDescription
                                if (canJoin) {
                                    role = Role.Button
                                    onClick(joinAction) {
                                        joinWithoutDrag()
                                        true
                                    }
                                }
                            }
                            .spliceKeyboardInput(canJoin, joinWithoutDrag)
                            .spliceInput(gesture, canJoin, onJoin),
                    ) {
                        with(artwork) {
                            drawPhotos(
                                load = download.value,
                                offset = gesture.offset,
                                lift = gesture.lift,
                                snap = snap.value,
                                isReady = isReady,
                                seconds = seconds,
                                anchor = gesture.anchor,
                            )
                        }
                    }
                }
            }
        }

        if (joined) {
            val finishedDescription = stringResource(R.string.splice_finished_description)
            var origin by remember { mutableStateOf(Offset.Zero) }
            Canvas(
                Modifier.fillMaxSize()
                    .onGloballyPositioned { origin = it.positionInRoot() }
                    .graphicsLayer { alpha = (reveal.value * 3f).coerceAtMost(1f) }
                    .then(if (finished) Modifier.semantics { contentDescription = finishedDescription } else Modifier),
            ) {
                val progress = reveal.value
                if (progress <= 0f) return@Canvas
                val width = minOf(size.width * 1.18f, size.height * SpliceLayout.PhotoAspect)
                val height = width / SpliceLayout.PhotoAspect
                val destination = Rect(Offset((size.width - width) / 2f, (size.height - height) / 2f), Size(width, height))
                val start = if (photosInRoot.isEmpty) destination else Rect(
                    photosInRoot.topLeft - origin,
                    Size(photosInRoot.width * (SpliceLayout.room.width / SpliceLayout.Width), photosInRoot.height),
                )
                val photo = lerp(start, destination, progress)
                drawRect(C.Night.copy(alpha = (progress * 2f).coerceAtMost(1f)))
                with(artwork) { drawRoom(photo, seconds) }
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to C.Night,
                        0.17f to C.Night.copy(alpha = 0f),
                        0.86f to C.Night.copy(alpha = 0f),
                        1f to C.Night,
                        startY = photo.top,
                        endY = photo.bottom,
                    ),
                    topLeft = photo.topLeft,
                    size = photo.size,
                    alpha = progress,
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 18.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SpliceControl(stringResource(R.string.splice_back), canInteract, onBack, back = true)
            if (finished) {
                SpliceControl(stringResource(R.string.splice_replay), canInteract, onReplay)
            }
        }
    }
}

@Composable
private fun DesktopWallpaper(modifier: Modifier) {
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(C.Night, C.Desktop, C.Night)))
        val spacing = 4.dp.toPx()
        val line = C.Grid.copy(alpha = 0.10f)
        repeat((size.height / spacing).toInt()) {
            val y = it * spacing
            drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
    }
}

@Composable
private fun SpliceControl(label: String, enabled: Boolean, action: () -> Unit, back: Boolean = false) {
    Row(
        Modifier.heightIn(min = 48.dp)
            .background(C.BlueDark)
            .border(1.dp, C.ChromeEdge)
            .clickable(enabled = enabled, role = Role.Button, onClick = action)
            .padding(horizontal = 13.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (back) {
            val direction = LocalLayoutDirection.current
            GlyphView(
                Glyph.Back,
                C.Snow,
                Modifier.size(17.dp).graphicsLayer {
                    scaleX = if (direction == LayoutDirection.Rtl) -1f else 1f
                },
            )
        }
        Text(label, color = C.Snow, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}

@Composable
internal fun PageSpliceCover(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.splice_cover),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}
