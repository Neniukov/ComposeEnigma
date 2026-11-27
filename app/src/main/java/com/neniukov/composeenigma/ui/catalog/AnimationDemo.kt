package com.neniukov.composeenigma.ui.catalog

import androidx.annotation.StringRes
import com.neniukov.composeenigma.R

internal enum class AnimationDemo(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    @param:StringRes val category: Int,
) {
    SignalLock(
        title = R.string.signal_title,
        description = R.string.signal_description,
        category = R.string.signal_category,
    ),
    PageSplice(
        title = R.string.splice_title,
        description = R.string.splice_description,
        category = R.string.splice_category,
    ),
    SandPendulum(
        title = R.string.sand_title,
        description = R.string.sand_description,
        category = R.string.sand_category,
    ),
    NeuralCosmos(
        title = R.string.cosmos_title,
        description = R.string.cosmos_description,
        category = R.string.cosmos_category,
    ),
    PaperWorlds(
        title = R.string.paper_title,
        description = R.string.paper_description,
        category = R.string.paper_category,
    ),
    HelloAndroid(
        title = R.string.droid_title,
        description = R.string.droid_description,
        category = R.string.droid_category,
    ),
    KineticVinyl(
        title = R.string.vinyl_title,
        description = R.string.vinyl_description,
        category = R.string.vinyl_category,
    ),
    LiquidPortal(
        title = R.string.portal_title,
        description = R.string.portal_description,
        category = R.string.portal_category,
    ),
    Journey(
        title = R.string.journey_title,
        description = R.string.journey_description,
        category = R.string.journey_category,
    ),
}
