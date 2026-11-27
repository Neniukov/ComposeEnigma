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
import com.neniukov.composeenigma.ui.cosmos.CosmosRoute
import com.neniukov.composeenigma.ui.cosmos.navigation.CosmosNavKey
import com.neniukov.composeenigma.ui.droid.DroidRoute
import com.neniukov.composeenigma.ui.droid.navigation.DroidNavKey
import com.neniukov.composeenigma.ui.journey.JourneyRoute
import com.neniukov.composeenigma.ui.journey.navigation.JourneyNavKey
import com.neniukov.composeenigma.ui.paper.PaperRoute
import com.neniukov.composeenigma.ui.paper.navigation.PaperNavKey
import com.neniukov.composeenigma.ui.portal.LiquidPortalRoute
import com.neniukov.composeenigma.ui.portal.navigation.PortalNavKey
import com.neniukov.composeenigma.ui.sand.SandPendulumRoute
import com.neniukov.composeenigma.ui.sand.navigation.SandNavKey
import com.neniukov.composeenigma.ui.signal.SignalLockRoute
import com.neniukov.composeenigma.ui.signal.navigation.SignalNavKey
import com.neniukov.composeenigma.ui.splice.PageSpliceRoute
import com.neniukov.composeenigma.ui.splice.navigation.SpliceNavKey
import com.neniukov.composeenigma.ui.vinyl.KineticVinylRoute
import com.neniukov.composeenigma.ui.vinyl.navigation.VinylNavKey
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
                val openSignal = dropUnlessResumed {
                    if (backStack.lastOrNull() == CatalogNavKey.Catalog) {
                        backStack.add(SignalNavKey.SignalLock)
                    }
                }
                val openSplice = dropUnlessResumed {
                    if (backStack.lastOrNull() == CatalogNavKey.Catalog) {
                        backStack.add(SpliceNavKey.PageSplice)
                    }
                }
                val openSand = dropUnlessResumed {
                    if (backStack.lastOrNull() == CatalogNavKey.Catalog) {
                        backStack.add(SandNavKey.SandPendulum)
                    }
                }
                val openCosmos = dropUnlessResumed {
                    if (backStack.lastOrNull() == CatalogNavKey.Catalog) {
                        backStack.add(CosmosNavKey.NeuralCosmos)
                    }
                }
                val openPaper = dropUnlessResumed {
                    if (backStack.lastOrNull() == CatalogNavKey.Catalog) {
                        backStack.add(PaperNavKey.PaperWorlds)
                    }
                }
                val openDroid = dropUnlessResumed {
                    if (backStack.lastOrNull() == CatalogNavKey.Catalog) {
                        backStack.add(DroidNavKey.HelloAndroid)
                    }
                }
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
                val openVinyl = dropUnlessResumed {
                    if (backStack.lastOrNull() == CatalogNavKey.Catalog) {
                        backStack.add(VinylNavKey.KineticVinyl)
                    }
                }
                AnimationCatalogScreen(
                    animations = AnimationDemo.entries,
                    onOpen = { demo ->
                        when (demo) {
                            AnimationDemo.SignalLock -> openSignal()
                            AnimationDemo.PageSplice -> openSplice()
                            AnimationDemo.SandPendulum -> openSand()
                            AnimationDemo.NeuralCosmos -> openCosmos()
                            AnimationDemo.PaperWorlds -> openPaper()
                            AnimationDemo.HelloAndroid -> openDroid()
                            AnimationDemo.KineticVinyl -> openVinyl()
                            AnimationDemo.LiquidPortal -> openPortal()
                            AnimationDemo.Journey -> openJourney()
                        }
                    },
                )
            }
            entry<SignalNavKey.SignalLock> {
                SignalLockRoute(
                    isActive = backStack.lastOrNull() == SignalNavKey.SignalLock,
                    onBack = dropUnlessResumed {
                        if (backStack.lastOrNull() == SignalNavKey.SignalLock) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                )
            }
            entry<SpliceNavKey.PageSplice> {
                PageSpliceRoute(
                    isActive = backStack.lastOrNull() == SpliceNavKey.PageSplice,
                    onBack = dropUnlessResumed {
                        if (backStack.lastOrNull() == SpliceNavKey.PageSplice) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                )
            }
            entry<SandNavKey.SandPendulum> {
                SandPendulumRoute(
                    isActive = backStack.lastOrNull() == SandNavKey.SandPendulum,
                    onBack = dropUnlessResumed {
                        if (backStack.lastOrNull() == SandNavKey.SandPendulum) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                )
            }
            entry<CosmosNavKey.NeuralCosmos> {
                CosmosRoute(
                    isActive = backStack.lastOrNull() == CosmosNavKey.NeuralCosmos,
                    onBack = dropUnlessResumed {
                        if (backStack.lastOrNull() == CosmosNavKey.NeuralCosmos) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                )
            }
            entry<PaperNavKey.PaperWorlds> {
                PaperRoute(
                    isActive = backStack.lastOrNull() == PaperNavKey.PaperWorlds,
                    onBack = dropUnlessResumed {
                        if (backStack.lastOrNull() == PaperNavKey.PaperWorlds) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                )
            }
            entry<DroidNavKey.HelloAndroid> {
                DroidRoute(
                    isActive = backStack.lastOrNull() == DroidNavKey.HelloAndroid,
                    onBack = dropUnlessResumed {
                        if (backStack.lastOrNull() == DroidNavKey.HelloAndroid) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                )
            }
            entry<VinylNavKey.KineticVinyl> {
                KineticVinylRoute(
                    isActive = backStack.lastOrNull() == VinylNavKey.KineticVinyl,
                    onBack = dropUnlessResumed {
                        if (backStack.lastOrNull() == VinylNavKey.KineticVinyl) {
                            backStack.removeAt(backStack.lastIndex)
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
