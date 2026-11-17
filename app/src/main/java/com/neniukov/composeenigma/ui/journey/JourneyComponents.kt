package com.neniukov.composeenigma.ui.journey

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neniukov.composeenigma.ui.components.Glyph
import com.neniukov.composeenigma.ui.components.GlyphView
import com.neniukov.composeenigma.ui.theme.JourneyColors as C

@Composable
internal fun Eyebrow(@StringRes resource: Int, color: Color, size: Int) {
    Text(
        stringResource(resource), fontSize = size.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.7.sp,
        color = color, maxLines = 1
    )
}

@Composable
internal fun CircleControl(
    kind: Glyph,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val semantics = if (enabled) Modifier else Modifier.clearAndSetSemantics {}
    Box(
        modifier
            .then(semantics)
            .size(48.dp)
            .clip(CircleShape)
            .background(C.Glass, CircleShape)
            .border(.75.dp, C.White.copy(alpha = .35f), CircleShape)
            .semantics { contentDescription = label }
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(14.dp)
    ) {
        GlyphView(kind, C.White, Modifier.fillMaxSize())
    }
}
