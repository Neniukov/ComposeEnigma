package com.neniukov.composeenigma.ui.sand.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface SandNavKey : NavKey {
    @Serializable
    data object SandPendulum : SandNavKey
}
