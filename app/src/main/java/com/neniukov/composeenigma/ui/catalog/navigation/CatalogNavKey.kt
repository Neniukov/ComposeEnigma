package com.neniukov.composeenigma.ui.catalog.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface CatalogNavKey : NavKey {
    @Serializable
    @SerialName("catalog")
    data object Catalog : CatalogNavKey
}
