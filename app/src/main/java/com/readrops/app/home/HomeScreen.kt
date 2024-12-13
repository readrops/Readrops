package com.readrops.app.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.readrops.app.R
import com.readrops.app.account.AccountTab
import com.readrops.app.feeds.FeedTab
import com.readrops.app.more.MoreTab
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

        TabNavigator(
            tab = TimelineTab
        ) { tabNavigator ->
            CompositionLocalProvider(LocalNavigator provides navigator) {
                Scaffold(
                    bottomBar = {
                        BottomAppBar {
                            NavigationBarItem(
                                selected = tabNavigator.current.key == TimelineTab.key,
                                onClick = { tabNavigator.current = TimelineTab },
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_timeline),
                                        contentDescription = null
                                    )
                                },
                                label = { Text(stringResource(id = R.string.timeline)) }
                            )

                            NavigationBarItem(
                                selected = tabNavigator.current.key == FeedTab.key,
                                onClick = { tabNavigator.current = FeedTab },
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_rss_feed_grey),
                                        contentDescription = null
                                    )
                                },
                                label = { Text(text = stringResource(R.string.feeds)) }
                            )

                            NavigationBarItem(
                                selected = tabNavigator.current.key == AccountTab.key,
                                onClick = { tabNavigator.current = AccountTab },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(text = stringResource(R.string.account)) }
                            )

                            NavigationBarItem(
                                selected = tabNavigator.current.key == MoreTab.key,
                                onClick = { tabNavigator.current = MoreTab },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(stringResource(id = R.string.more)) }
                            )
                        }
                    },
                    contentWindowInsets = scaffoldInsets
                ) { paddingValues ->
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues)
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