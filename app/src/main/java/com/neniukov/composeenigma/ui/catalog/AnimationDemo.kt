package com.neniukov.composeenigma.ui.catalog

import androidx.annotation.StringRes
import com.neniukov.composeenigma.R

internal enum class AnimationDemo(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    @param:StringRes val category: Int,
    @param:StringRes val coverTitle: Int,
) {
    HelloAndroid(
        title = R.string.droid_title,
        description = R.string.droid_description,
        category = R.string.droid_category,
        coverTitle = R.string.droid_cover_title,
    ),
    KineticVinyl(
        title = R.string.vinyl_title,
        description = R.string.vinyl_description,
        category = R.string.vinyl_category,
        coverTitle = R.string.vinyl_cover_title,
    ),
    LiquidPortal(
        title = R.string.portal_title,
        description = R.string.portal_description,
        category = R.string.portal_category,
        coverTitle = R.string.portal_cover_title,
    ),
    Journey(
        title = R.string.journey_title,
        description = R.string.journey_description,
        category = R.string.journey_category,
        coverTitle = R.string.destination,
    ),
}
