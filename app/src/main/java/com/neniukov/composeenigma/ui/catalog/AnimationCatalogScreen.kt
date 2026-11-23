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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.cosmos.CosmosCover
import com.neniukov.composeenigma.ui.droid.DroidCover
import com.neniukov.composeenigma.ui.paper.PaperCover
import com.neniukov.composeenigma.ui.portal.LiquidPortalCover
import com.neniukov.composeenigma.ui.theme.MotionStudiesTheme
import com.neniukov.composeenigma.ui.vinyl.KineticVinylCover
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
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
            CatalogHeader()
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.catalog_collection),
                    color = C.Muted,
                    fontSize = 9.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 1.6.sp,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    pluralStringResource(R.plurals.catalog_study_count, animations.size, animations.size),
                    color = C.Accent,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                animations.forEach { demo ->
                    key(demo.name) {
                        AnimationCard(demo = demo, onOpen = { onOpen(demo) })
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.catalog_footer),
                color = C.Muted,
                fontSize = 10.sp,
                lineHeight = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
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
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-1.4).sp,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        Box(Modifier.size(40.dp).border(1.dp, C.Line, CircleShape), contentAlignment = Alignment.Center) {
            GlyphView(Glyph.Compass, C.Accent, Modifier.size(22.dp))
        }
    }
    Spacer(Modifier.height(18.dp))
    Text(
        stringResource(R.string.catalog_headline),
        color = C.Ink,
        fontFamily = FontFamily.Serif,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1.4).sp,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.catalog_description),
        color = C.Muted,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    )
}

@Composable
private fun AnimationCard(demo: AnimationDemo, onOpen: () -> Unit) {
    val title = stringResource(demo.title)
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(C.White)
            .border(1.dp, C.Line.copy(alpha = 0.7f), shape)
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.catalog_open_animation, title),
                onClick = onOpen,
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(13.dp)).background(C.Sage)) {
            when (demo) {
                AnimationDemo.NeuralCosmos -> CosmosCover(Modifier.fillMaxSize())
                AnimationDemo.PaperWorlds -> PaperCover(Modifier.fillMaxSize())
                AnimationDemo.HelloAndroid -> DroidCover(Modifier.fillMaxSize())
                AnimationDemo.KineticVinyl -> KineticVinylCover(Modifier.fillMaxSize())
                AnimationDemo.LiquidPortal -> LiquidPortalCover(Modifier.fillMaxSize())
                AnimationDemo.Journey -> Image(
                    painterResource(R.drawable.dolomites),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f).padding(vertical = 2.dp)) {
            Text(
                stringResource(demo.category),
                color = C.Accent,
                fontSize = 7.sp,
                lineHeight = 11.sp,
                letterSpacing = 0.7.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                color = C.Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                letterSpacing = (-0.4).sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(demo.description),
                color = C.Muted,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        GlyphView(Glyph.Arrow, C.Accent, Modifier.size(18.dp))
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
