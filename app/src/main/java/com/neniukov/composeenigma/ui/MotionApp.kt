package com.neniukov.composeenigma.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.neniukov.composeenigma.ui.catalog.AnimationCatalogScreen
import com.neniukov.composeenigma.ui.catalog.AnimationDemo
import com.neniukov.composeenigma.ui.catalog.navigation.CatalogNavKey
import com.neniukov.composeenigma.ui.journey.JourneyRoute
import com.neniukov.composeenigma.ui.journey.navigation.JourneyNavKey
import com.neniukov.composeenigma.ui.portal.LiquidPortalRoute
import com.neniukov.composeenigma.ui.portal.navigation.PortalNavKey
import com.neniukov.composeenigma.ui.theme.JourneyColors as C

@Composable
internal fun MotionApp() {
    val backStack = rememberNavBackStack(CatalogNavKey.Catalog)
    var journeySaved by rememberSaveable { mutableStateOf(false) }

    NavDisplay(
        backStack = backStack,
        modifier = Modifier
            .fillMaxSize()
            .background(C.Paper),
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        sceneStrategies = listOf(SinglePaneSceneStrategy()),
        entryProvider = entryProvider {
            entry<CatalogNavKey.Catalog> {
                val openJourney = dropUnlessResumed {
                    if (backStack.lastOrNull() == CatalogNavKey.Catalog) {
                        backStack.add(JourneyNavKey.Journey)
                    }
                }
                val openPortal = dropUnlessResumed {
                    if (backStack.lastOrNull() == CatalogNavKey.Catalog) {
                        backStack.add(PortalNavKey.LiquidPortal)
                    }
                }
                AnimationCatalogScreen(
                    animations = AnimationDemo.entries,
                    onOpen = { demo ->
                        when (demo) {
                            AnimationDemo.LiquidPortal -> openPortal()
                            AnimationDemo.Journey -> openJourney()
                        }
                    },
                )
            }
            entry<PortalNavKey.LiquidPortal> {
                LiquidPortalRoute(
                    isActive = backStack.lastOrNull() == PortalNavKey.LiquidPortal,
                    onBack = dropUnlessResumed {
                        if (backStack.lastOrNull() == PortalNavKey.LiquidPortal) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                )
            }
            entry<JourneyNavKey.Journey> {
                JourneyRoute(
                    saved = journeySaved,
                    onSave = { journeySaved = !journeySaved },
                    isActive = backStack.lastOrNull() == JourneyNavKey.Journey,
                )
            }
        },
    )
}
