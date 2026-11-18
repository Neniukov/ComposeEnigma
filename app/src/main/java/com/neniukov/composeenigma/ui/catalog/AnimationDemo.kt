package com.neniukov.composeenigma.ui.catalog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.neniukov.composeenigma.R

internal enum class AnimationDemo(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    @param:StringRes val category: Int,
    @param:StringRes val coverTitle: Int,
    @param:DrawableRes val cover: Int,
) {
    LiquidPortal(
        title = R.string.portal_title,
        description = R.string.portal_description,
        category = R.string.portal_category,
        coverTitle = R.string.portal_cover_title,
        cover = R.drawable.portal_desert,
    ),
    Journey(
        title = R.string.journey_title,
        description = R.string.journey_description,
        category = R.string.journey_category,
        coverTitle = R.string.destination,
        cover = R.drawable.dolomites,
    ),
}
