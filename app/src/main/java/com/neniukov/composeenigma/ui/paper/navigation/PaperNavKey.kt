package com.neniukov.composeenigma.ui.paper.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface PaperNavKey : NavKey {
    @Serializable
    data object PaperWorlds : PaperNavKey
}
