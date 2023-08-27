package com.readrops.app.compose.timelime

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.readrops.app.compose.R
import com.readrops.app.compose.item.ItemScreen
import com.readrops.app.compose.timelime.drawer.TimelineDrawer
import com.readrops.app.compose.util.theme.spacing
import org.koin.androidx.compose.getViewModel


object TimelineTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Timeline",
        )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = getViewModel<TimelineViewModel>()
        val state by viewModel.timelineState.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        val scrollState = rememberLazyListState()
        val swipeState = rememberSwipeRefreshState(state.isRefreshing)
        val drawerState = rememberDrawerState(
            initialValue = DrawerValue.Closed,
            confirmStateChange = {
                if (it == DrawerValue.Closed) {
                    viewModel.closeDrawer()
                } else {
                    viewModel.openDrawer()
                }

                true
            }
        )

        BackHandler(
            enabled = state.isDrawerOpen,
            onBack = { viewModel.closeDrawer() }
        )

        LaunchedEffect(state.isDrawerOpen) {
            if (state.isDrawerOpen) {
                drawerState.open()
            } else {
                drawerState.close()
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                TimelineDrawer(
                    state = state,
                    onClickDefaultItem = {
                        viewModel.updateDrawerDefaultItem(it)
                    },
                    onFolderClick = {
                        viewModel.updateDrawerFolderSelection(it)
                    },
                    onFeedClick = {
                        viewModel.updateDrawerFeedSelection(it)
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = "Articles") },
                        navigationIcon = {
                            IconButton(
                                onClick = { viewModel.openDrawer() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_filter_list),
                                    contentDescription = null
                                )
                            }

                            IconButton(onClick = { }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sync),
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_done_all),
                            contentDescription = null
                        )
                    }
                },
            ) { paddingValues ->
                SwipeRefresh(
                    state = swipeState,
                    onRefresh = { viewModel.refreshTimeline() },
                    modifier = Modifier.padding(paddingValues)
                ) {
                    when (val itemState = state.items) {
                        is ItemState.Loading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is ItemState.Error -> TODO()
                        is ItemState.Loaded -> {
                            val items = itemState.items.collectAsLazyPagingItems()

                            LazyColumn(
                                state = scrollState,
                                contentPadding = PaddingValues(vertical = MaterialTheme.spacing.shortSpacing),
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.shortSpacing)
                            ) {
                                items(
                                    count = items.itemCount,
                                    key = { items[it]!!.item.id },
                                    contentType = { "item_with_feed" }
                                ) { itemCount ->
                                    val itemWithFeed = items[itemCount]!!

                                    TimelineItem(
                                        itemWithFeed = itemWithFeed,
                                        onClick = {
                                            viewModel.setItemRead(itemWithFeed.item)
                                            navigator.push(ItemScreen())
                                        },
                                        onFavorite = { viewModel.updateStarState(itemWithFeed.item) },
                                        onReadLater = {},
                                        onShare = {
                                            viewModel.shareItem(itemWithFeed.item, context)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoItemPlaceholder() {
    val scrollState = rememberScrollState()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "No item",
            style = MaterialTheme.typography.displayMedium
        )
    }
}