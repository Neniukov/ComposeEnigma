package com.neniukov.composeenigma.ui.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme
import com.neniukov.composeenigma.ui.theme.JourneyColors as C

@Composable
internal fun AnimationCatalogScreen(
    animations: List<AnimationDemo>,
    onOpen: (AnimationDemo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(C.Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
            CatalogHeader()
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.catalog_collection),
                    color = C.Muted,
                    fontSize = 10.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 1.8.sp,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    pluralStringResource(R.plurals.catalog_study_count, animations.size, animations.size),
                    color = C.Accent,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                animations.forEach { demo ->
                    key(demo.name) {
                        AnimationCard(demo = demo, onOpen = { onOpen(demo) })
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.catalog_footer),
                color = C.Muted,
                fontSize = 11.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun CatalogHeader() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.catalog_brand),
            color = C.Ink,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-1.8).sp,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        GlyphView(Glyph.Compass, C.Accent, Modifier.size(32.dp))
    }
    Spacer(Modifier.height(34.dp))
    Text(
        stringResource(R.string.catalog_headline),
        color = C.Ink,
        fontFamily = FontFamily.Serif,
        fontSize = 54.sp,
        lineHeight = 56.sp,
        letterSpacing = (-2).sp,
    )
    Spacer(Modifier.height(16.dp))
    Text(
        stringResource(R.string.catalog_description),
        color = C.Muted,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        modifier = Modifier.widthIn(max = 320.dp),
    )
}

@Composable
private fun AnimationCard(demo: AnimationDemo, onOpen: () -> Unit) {
    val title = stringResource(demo.title)
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(C.White)
            .border(1.dp, C.Line, shape)
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.catalog_open_animation, title),
                onClick = onOpen,
            ),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.5f)) {
            Image(
                painterResource(demo.cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, C.PhotoShade.copy(alpha = .75f))),
                ),
            )
            Text(
                stringResource(demo.coverTitle),
                color = C.White,
                fontFamily = FontFamily.Serif,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                letterSpacing = (-.8).sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
            )
        }
        Column(Modifier.padding(20.dp)) {
            Text(
                stringResource(demo.category),
                color = C.Accent,
                fontSize = 9.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(title, color = C.Ink, fontFamily = FontFamily.Serif, fontSize = 26.sp, lineHeight = 32.sp)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(demo.description), color = C.Muted, fontSize = 13.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.catalog_explore),
                    color = C.Ink,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(16.dp))
                Box(Modifier.size(40.dp).background(C.Accent, CircleShape), contentAlignment = Alignment.Center) {
                    GlyphView(Glyph.Arrow, C.White, Modifier.size(20.dp))
                }
            }
        }
    }
}

@Preview(name = "Catalog", widthDp = 393, heightDp = 852, apiLevel = 36)
@Preview(name = "Catalog short", widthDp = 360, heightDp = 640, apiLevel = 36)
@Preview(name = "Catalog large text", widthDp = 393, heightDp = 852, fontScale = 2f, apiLevel = 36)
@Preview(name = "Catalog wide", widthDp = 1000, heightDp = 800, apiLevel = 36)
@Composable
private fun AnimationCatalogPreview() {
    MotionStudiesTheme {
        AnimationCatalogScreen(animations = AnimationDemo.entries, onOpen = {})
    }
}
