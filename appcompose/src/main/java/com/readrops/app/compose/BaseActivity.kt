package com.readrops.app.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.readrops.app.compose.account.AccountScreen
import com.readrops.app.compose.feeds.FeedsScreen
import com.readrops.app.compose.more.MoreScreen
import com.readrops.app.compose.timelime.TimelineScreen

class BaseActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReadropsTheme {
                Navigator(screen = TimelineScreen()) { navigator ->
                    Scaffold(
                        bottomBar = {
                            BottomAppBar {
                                NavigationBarItem(
                                    selected = false,
                                    onClick = { navigator.push(TimelineScreen()) },
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
                                    onClick = { navigator.push(FeedsScreen()) },
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
                                    onClick = { navigator.push(AccountScreen()) },
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
                                    onClick = { navigator.push(MoreScreen()) },
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
                            CurrentScreen()
                        }
                    }
                }
            }
        }
    }
}