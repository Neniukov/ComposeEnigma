package com.neniukov.composeenigma.ui.cosmos.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface CosmosNavKey : NavKey {
    @Serializable
    data object NeuralCosmos : CosmosNavKey
}
