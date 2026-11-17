package com.neniukov.composeenigma.ui.journey

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.theme.JourneyColors as C

@Composable
internal fun Hero(
    progress: State<Float>,
    enabled: Boolean,
    saved: Boolean,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier,
) {
    val shape = remember(progress) { HeroShape(progress) }
    val openLabel = stringResource(R.string.open_escape)
    val collapsedSemantics = if (enabled) Modifier else Modifier.clearAndSetSemantics {}
    val openAction = if (enabled) {
        Modifier.clickable(onClickLabel = openLabel, role = Role.Button, onClick = onOpen)
    } else {
        Modifier
    }
    Box(
        modifier
            .graphicsLayer {
                this.shape = shape
                clip = true
            }
            .then(openAction),
    ) {
        Image(
            painterResource(R.drawable.dolomites), stringResource(R.string.photo_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val zoom = 1f + JourneyMotion.HERO_ZOOM * progress.value
                    scaleX = zoom
                    scaleY = zoom
                    translationY = -progress.value * JourneyMotion.HeroLift.toPx()
                })
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to C.PhotoShade.copy(alpha = .05f),
                        .46f to Color.Transparent,
                        1f to C.PhotoShade.copy(alpha = .78f)
                    )
                )
        )
        Box(
            Modifier
                .padding(18.dp)
                .then(collapsedSemantics)
                .graphicsLayer { alpha = 1f - JourneyMotion.CoordinatesExit.valueAt(progress.value) }) {
            Text(
                stringResource(R.string.coordinates), fontFamily = FontFamily.Monospace, fontSize = 9.sp,
                color = C.White, letterSpacing = .5.sp,
                modifier = Modifier
                    .background(C.PhotoShade.copy(alpha = .2f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        CircleControl(
            if (saved) Glyph.HeartFilled else Glyph.Heart,
            stringResource(if (saved) R.string.unsave_escape else R.string.save_escape), onSave,
            Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .graphicsLayer {
                    translationY = JourneyMotion.BookmarkDrop.toPx() * progress.value
                })
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 22.dp)
                .fillMaxWidth(1f / JourneyMotion.TITLE_SCALE)
                .graphicsLayer {
                    translationY = -JourneyMotion.TitleLift.toPx() * progress.value
                    val scale = mix(1f, JourneyMotion.TITLE_SCALE, progress.value)
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 1f)
                }) {
            Eyebrow(R.string.location, C.White.copy(alpha = .8f), 8)
            Spacer(Modifier.height(7.dp))
            Text(
                stringResource(R.string.destination), fontFamily = FontFamily.Serif,
                fontSize = 33.sp, letterSpacing = (-1.2).sp, lineHeight = 40.sp, color = C.White
            )
            Spacer(Modifier.height(5.dp))
            Text(
                stringResource(R.string.card_caption), color = C.White.copy(alpha = .8f), fontSize = 11.sp,
                modifier = Modifier
                    .then(collapsedSemantics)
                    .graphicsLayer {
                        alpha = 1f - JourneyMotion.CaptionExit.valueAt(progress.value)
                    },
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .graphicsLayer {
                    alpha = 1f - JourneyMotion.ArrowExit.valueAt(progress.value)
                    rotationZ = JourneyMotion.ARROW_ROTATION * progress.value
                }) { GlyphView(Glyph.Arrow, C.White, Modifier.size(26.dp)) }
    }
}
