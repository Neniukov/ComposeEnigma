package com.neniukov.composeenigma.ui.journey.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface JourneyNavKey : NavKey {
    @Serializable
    @SerialName("journey")
    data object Journey : JourneyNavKey
}
