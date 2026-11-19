package com.neniukov.composeenigma.ui.vinyl

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.theme.VinylColors as C

@Composable
internal fun KineticVinylRoute(isActive: Boolean, onBack: () -> Unit) {
    val motion = rememberVinylMotion(isActive)
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    KineticVinylScreen(
        motion = motion,
        interactive = isActive && lifecycleState == Lifecycle.State.RESUMED,
        onBack = onBack,
        onTogglePlayback = motion::togglePlayback,
        onEject = motion::eject,
        onSelectSpeed = motion::selectSpeed,
    )
}

@Composable
internal fun KineticVinylScreen(
    motion: VinylMotion,
    interactive: Boolean,
    onBack: () -> Unit,
    onTogglePlayback: () -> Unit,
    onEject: () -> Unit,
    onSelectSpeed: (VinylSpeed) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .vinylPaper()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val wide = maxWidth >= 760.dp
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = if (wide) 1_160.dp else 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            VinylHeader(onBack)
            Spacer(Modifier.height(if (wide) 24.dp else 28.dp))
            if (wide) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(0.9f)) {
                        VinylIntro()
                        Spacer(Modifier.height(36.dp))
                        VinylTransport(
                            playing = motion.playing,
                            loaded = motion.loaded,
                            speed = motion.speed,
                            enabled = interactive,
                            onTogglePlayback = onTogglePlayback,
                            onEject = onEject,
                            onSelectSpeed = onSelectSpeed,
                        )
                    }
                    Spacer(Modifier.width(24.dp))
                    VinylScene(motion, interactive, Modifier.weight(1.3f))
                }
            } else {
                VinylIntro()
                VinylScene(motion, interactive, Modifier.fillMaxWidth())
                VinylTransport(
                    playing = motion.playing,
                    loaded = motion.loaded,
                    speed = motion.speed,
                    enabled = interactive,
                    onTogglePlayback = onTogglePlayback,
                    onEject = onEject,
                    onSelectSpeed = onSelectSpeed,
                )
            }
            Spacer(Modifier.height(26.dp))
            VinylFooter()
        }
    }
}

@Composable
private fun VinylHeader(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val back = stringResource(R.string.vinyl_back)
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.dp, C.Line, CircleShape)
                .clickable(role = Role.Button, onClickLabel = back, onClick = onBack)
                .semantics { contentDescription = back },
            contentAlignment = Alignment.Center,
        ) {
            GlyphView(Glyph.Back, C.Ink, Modifier.size(20.dp))
        }
        Text(
            stringResource(R.string.vinyl_brand),
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            fontFamily = FontFamily.Serif,
            fontSize = 28.sp,
            letterSpacing = (-1.2).sp,
            color = C.Ink,
        )
        VinylCaption(R.string.vinyl_issue, accent = true)
    }
}

@Composable
private fun VinylIntro() {
    VinylCaption(R.string.vinyl_eyebrow, accent = true)
    Spacer(Modifier.height(12.dp))
    Text(
        stringResource(R.string.vinyl_headline),
        fontFamily = FontFamily.Serif,
        fontSize = 50.sp,
        lineHeight = 49.sp,
        letterSpacing = (-2).sp,
        color = C.Ink,
    )
}

@Composable
private fun VinylTransport(
    playing: Boolean,
    loaded: Boolean,
    speed: VinylSpeed,
    enabled: Boolean,
    onTogglePlayback: () -> Unit,
    onEject: () -> Unit,
    onSelectSpeed: (VinylSpeed) -> Unit,
) {
    Text(
        stringResource(R.string.vinyl_touch_hint),
        modifier = Modifier.fillMaxWidth(),
        color = C.Muted,
        fontSize = 9.sp,
        letterSpacing = 1.4.sp,
        lineHeight = 16.sp,
    )
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(C.Line))
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.vinyl_album),
                color = C.Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 25.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.6).sp,
            )
            Spacer(Modifier.height(4.dp))
            VinylCaption(R.string.vinyl_track)
        }
        Spacer(Modifier.width(12.dp))
        Row(
            Modifier
                .selectableGroup()
                .border(1.dp, C.Line, CircleShape)
                .padding(3.dp),
        ) {
            VinylSpeed.entries.forEach { option ->
                val selected = option == speed
                val speedDescription = stringResource(R.string.vinyl_speed_description, stringResource(option.label))
                Box(
                    Modifier
                        .widthIn(min = 48.dp)
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .background(if (selected) C.Ink else C.Paper)
                        .selectable(
                            selected = selected,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onSelectSpeed(option) },
                        )
                        .semantics { contentDescription = speedDescription }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(option.label),
                        style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                        fontFamily = FontFamily.Serif,
                        fontSize = 15.sp,
                        color = if (selected) C.Paper else C.Muted,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(22.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val rotating = loaded && playing
        val playLabel = stringResource(
            when {
                !loaded -> R.string.vinyl_load
                rotating -> R.string.vinyl_pause
                else -> R.string.vinyl_play
            },
        )
        Row(
            Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .clip(CircleShape)
                .background(C.Ink)
                .clickable(enabled = enabled, role = Role.Button, onClick = onTogglePlayback)
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            GlyphView(if (rotating) Glyph.Pause else Glyph.Play, C.Cream, Modifier.size(13.dp))
            Spacer(Modifier.width(12.dp))
            Text(playLabel, color = C.Paper, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
        }
        val eject = stringResource(R.string.vinyl_eject)
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(1.dp, C.Line, CircleShape)
                .clickable(enabled = enabled && loaded, role = Role.Button, onClick = onEject)
                .semantics { contentDescription = eject },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(20.dp)) {
                val color = if (loaded) C.Ink else C.Muted
                drawPath(
                    Path().apply {
                        moveTo(size.width * 0.5f, size.height * 0.15f)
                        lineTo(size.width * 0.88f, size.height * 0.6f)
                        lineTo(size.width * 0.12f, size.height * 0.6f)
                        close()
                    },
                    color,
                )
                drawLine(color, Offset(size.width * 0.12f, size.height * 0.85f), Offset(size.width * 0.88f, size.height * 0.85f), 2.dp.toPx())
            }
        }
    }
}

@Composable
private fun VinylFooter() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        VinylCaption(R.string.vinyl_footer, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        VinylCaption(R.string.vinyl_footer_side)
    }
}

@Composable
private fun VinylCaption(@StringRes label: Int, modifier: Modifier = Modifier, accent: Boolean = false) {
    Text(
        stringResource(label),
        modifier = modifier,
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
        color = if (accent) C.Accent else C.Muted,
    )
}
