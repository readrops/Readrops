package com.readrops.app.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.readrops.app.feeds.FeedTab
import com.readrops.app.timelime.TimelineTab
import com.readrops.app.util.components.AndroidScreen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object HomeScreen : AndroidScreen() {

    private val tabChannel = Channel<Tab>()

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scaffoldInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)

        val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
            adaptiveInfo = currentWindowAdaptiveInfo()
        )

        TabNavigator(
            tab = TimelineTab
        ) { tabNavigator ->
            CompositionLocalProvider(LocalNavigator provides navigator) {
                NavigationSuiteScaffoldLayout(
                    layoutType = layoutType,
                    navigationSuite =  {
                        if (layoutType == NavigationSuiteType.NavigationRail) {
                            NavigationRail(
                                windowInsets = scaffoldInsets,
                                containerColor = BottomAppBarDefaults.containerColor
                            ) {
                                Spacer(Modifier.weight(1f))

                                HomeTabs.entries.forEach {
                                    NavigationRailItem(
                                        selected = tabNavigator.current.key == it.tab.key,
                                        onClick = { tabNavigator.current = it.tab },
                                        icon = {
                                            Icon(
                                                painter = painterResource(it.iconRes),
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(stringResource(it.labelRes)) }
                                    )
                                }

                                Spacer(Modifier.weight(1f))
                            }
                        } else {
                            NavigationSuite(
                                layoutType = layoutType,
                            ) {
                                HomeTabs.entries.forEach {
                                    item(
                                        selected = tabNavigator.current.key == it.tab.key,
                                        onClick = { tabNavigator.current = it.tab },
                                        icon = {
                                            Icon(
                                                painter = painterResource(it.iconRes),
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(stringResource(it.labelRes)) }
                                    )
                                }
                            }
                        }
                    }
                ) {
                    LaunchedEffect(Unit) {
                        tabChannel.receiveAsFlow()
                            .collect {
                                tabNavigator.current = it
                            }
                    }

                    BackHandler(
                        enabled = tabNavigator.current != TimelineTab,
                        onBack = { tabNavigator.current = TimelineTab }
                    )

                    Box(
                        modifier = Modifier.fillMaxSize().run {
                            // Navigation bar already applies bottom inset, so make sure that tabs don't apply it too
                            if (layoutType == NavigationSuiteType.NavigationBar) {
                                consumeWindowInsets(ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom))
                            } else {
                                this
                            }
                        }
                    ) {
                        CurrentTab()
                    }
                }
            }
        }
    }

    suspend fun openItem(itemId: Int) {
        tabChannel.send(TimelineTab)
        TimelineTab.openItem(itemId)
    }

    suspend fun openTab(tab: Tab) {
        tabChannel.send(tab)
    }

    suspend fun openAddFeedDialog(url: String) {
        tabChannel.send(FeedTab)
        FeedTab.openAddFeedDialog(url)
    }
}