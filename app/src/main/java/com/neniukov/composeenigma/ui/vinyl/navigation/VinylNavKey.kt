package com.neniukov.composeenigma.ui.vinyl.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface VinylNavKey : NavKey {
    @Serializable
    @SerialName("kinetic_vinyl")
    data object KineticVinyl : VinylNavKey
}
