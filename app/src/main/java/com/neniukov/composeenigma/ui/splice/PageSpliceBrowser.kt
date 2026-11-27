package com.neniukov.composeenigma.ui.splice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.composeenigma.R
import com.neniukov.composeenigma.ui.theme.SpliceColors as C

@Composable
internal fun PageSpliceBrowser(
    isReady: Boolean,
    progress: () -> Float,
    modifier: Modifier = Modifier,
    photos: @Composable () -> Unit,
) {
    Column(
        modifier.background(C.Silver).border(1.dp, C.ChromeLight).padding(4.dp),
    ) {
        Row(
            Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(C.BlueDark, C.Blue)))
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Canvas(Modifier.size(12.dp)) {
                drawRect(C.Snow, Offset(size.width * 0.12f, size.height * 0.17f), size * 0.74f)
                drawRect(C.BlueDark, Offset(size.width * 0.21f, size.height * 0.39f), Size(size.width * 0.56f, size.height * 0.42f))
            }
            Text(
                stringResource(R.string.splice_browser_title),
                color = C.ChromeLight,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 17.sp,
            )
        }

        Box(
            Modifier.padding(vertical = 4.dp).fillMaxWidth()
                .background(C.ChromeLight).border(1.dp, C.ChromeEdge).padding(horizontal = 7.dp, vertical = 4.dp),
        ) {
            Text(
                stringResource(R.string.splice_address),
                color = C.Muted,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
        }

        Column(
            Modifier.fillMaxWidth().background(C.Parchment).border(1.dp, C.ChromeEdge)
                .padding(horizontal = 10.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.splice_greeting),
                modifier = Modifier.semantics { heading() },
                color = C.Red,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 31.sp,
                textAlign = TextAlign.Center,
            )
            photos()
            // The two photos are drawn left-to-right in one canvas.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PhotoLabel(stringResource(R.string.splice_room_file), Modifier.weight(1f))
                    PhotoLabel(stringResource(R.string.splice_yard_file), Modifier.weight(1f))
                }
            }
        }

        Column(
            Modifier.fillMaxWidth().padding(top = 4.dp)
                .border(1.dp, C.ChromeEdge).padding(horizontal = 8.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                stringResource(if (isReady) R.string.splice_drag_hint else R.string.splice_loading),
                Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                color = C.Ink,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp,
            )
            Canvas(Modifier.fillMaxWidth().height(6.dp)) {
                drawRect(C.ChromeLight)
                val segments = 26
                val step = size.width / segments
                val downloaded = (progress() * 0.84f * segments).toInt()
                repeat(downloaded) { index ->
                    drawRect(C.Blue, Offset(step * index, 0f), Size(step * 0.76f, size.height))
                }
            }
        }
    }
}

@Composable
private fun PhotoLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        label,
        modifier,
        color = C.Muted,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        textAlign = TextAlign.Center,
    )
}
