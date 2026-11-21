package com.neniukov.composeenigma.ui.droid.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface DroidNavKey : NavKey {
    @Serializable
    @SerialName("hello_android")
    data object HelloAndroid : DroidNavKey
}
