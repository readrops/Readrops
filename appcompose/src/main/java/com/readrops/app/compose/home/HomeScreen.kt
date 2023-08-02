package com.readrops.app.compose.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.readrops.app.compose.R
import com.readrops.app.compose.account.AccountTab
import com.readrops.app.compose.feeds.FeedTab
import com.readrops.app.compose.more.MoreTab
import com.readrops.app.compose.timelime.TimelineTab

class HomeScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        TabNavigator(
                tab = TimelineTab
        ) { tabNavigator ->
            CompositionLocalProvider(LocalNavigator provides navigator) {
                Scaffold(
                        bottomBar = {
                            BottomAppBar {
                                NavigationBarItem(
                                        selected = false,
                                        onClick = { tabNavigator.current = TimelineTab },
                                        icon = {
                                            Icon(
                                                    painter = painterResource(R.drawable.ic_timeline),
                                                    contentDescription = null
                                            )
                                        },
                                        label = { Text("Timeline") }
                                )

                                NavigationBarItem(
                                        selected = false,
                                        onClick = { tabNavigator.current = FeedTab },
                                        icon = {
                                            Icon(
                                                    painter = painterResource(R.drawable.ic_rss_feed_grey),
                                                    contentDescription = null
                                            )
                                        },
                                        label = { Text("Feeds") }
                                )

                                NavigationBarItem(
                                        selected = false,
                                        onClick = { tabNavigator.current = AccountTab },
                                        icon = {
                                            Icon(
                                                    imageVector = Icons.Default.AccountBox,
                                                    contentDescription = null,
                                            )
                                        },
                                        label = { Text("Account") }
                                )

                                NavigationBarItem(
                                        selected = false,
                                        onClick = { tabNavigator.current = MoreTab },
                                        icon = {
                                            Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = null,
                                            )
                                        },
                                        label = { Text("More") }
                                )
                            }
                        },
                ) { paddingValues ->
                    Box(
                            modifier = Modifier.padding(paddingValues)
                    ) {
                        CurrentTab()
                    }
                }
            }

        }
    }
}