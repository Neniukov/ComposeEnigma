package com.neniukov.composeenigma.ui.splice.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface SpliceNavKey : NavKey {
    @Serializable
    data object PageSplice : SpliceNavKey
}
