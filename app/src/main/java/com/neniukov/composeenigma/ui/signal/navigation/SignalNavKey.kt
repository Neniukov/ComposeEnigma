package com.neniukov.composeenigma.ui.signal.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface SignalNavKey : NavKey {
    @Serializable
    data object SignalLock : SignalNavKey
}
