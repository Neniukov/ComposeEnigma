package com.neniukov.composeenigma.ui.portal.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface PortalNavKey : NavKey {
    @Serializable
    @SerialName("liquid_portal")
    data object LiquidPortal : PortalNavKey
}
