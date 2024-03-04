package com.readrops.app.compose.timelime

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.readrops.app.compose.R
import com.readrops.app.compose.item.ItemScreen
import com.readrops.app.compose.timelime.drawer.TimelineDrawer
import com.readrops.app.compose.util.components.CenteredColumn
import com.readrops.app.compose.util.components.TwoChoicesDialog
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.filters.MainFilter
import com.readrops.db.filters.SubFilter


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
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        val viewModel = navigator.getNavigatorScreenModel<TimelineScreenModel>()

        val state by viewModel.timelineState.collectAsStateWithLifecycle()
        val items = state.itemState.collectAsLazyPagingItems()

        val scrollState = rememberLazyListState()
        val swipeState = rememberPullToRefreshState()

        LaunchedEffect(state.isRefreshing) {
            if (state.isRefreshing) {
                swipeState.startRefresh()
            } else {
                swipeState.endRefresh()
            }
        }

        // Material3 pull to refresh doesn't have a onRefresh callback,
        // so we need to listen to the internal state change to trigger the refresh
        LaunchedEffect(swipeState.isRefreshing) {
            if (swipeState.isRefreshing) {
                viewModel.refreshTimeline()
            }
        }

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

        when (state.dialog) {
            DialogState.ConfirmDialog -> {
                TwoChoicesDialog(
                    title = "Mark all items as read",
                    text = "Do you really want to mark all items as read?",
                    icon = painterResource(id = R.drawable.ic_rss_feed_grey),
                    confirmText = "Validate",
                    dismissText = "Cancel",
                    onDismiss = { viewModel.closeDialog() },
                    onConfirm = {
                        viewModel.closeDialog()
                        viewModel.setAllItemsRead()
                    }
                )
            }

            DialogState.FilterSheet -> {
                FilterBottomSheet(
                    viewModel = viewModel,
                    filters = state.filters,
                    onDismiss = {
                        viewModel.closeDialog()
                    }
                )
            }

            null -> {}
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
                        title = {
                            Text(
                                text = when (state.filters.subFilter) {
                                    SubFilter.FEED -> state.filterFeedName
                                    SubFilter.FOLDER -> state.filterFolderName
                                    else -> when (state.filters.mainFilter) {
                                        MainFilter.STARS -> stringResource(R.string.favorites)
                                        MainFilter.ALL -> stringResource(R.string.articles)
                                        MainFilter.NEW -> stringResource(R.string.new_articles)
                                    }
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
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
                            IconButton(
                                onClick = { viewModel.openDialog(DialogState.FilterSheet) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_filter_list),
                                    contentDescription = null
                                )
                            }

                            IconButton(
                                onClick = { viewModel.refreshTimeline() }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sync),
                                    contentDescription = null
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (state.filters.mainFilter == MainFilter.ALL) {
                                viewModel.openDialog(DialogState.ConfirmDialog)
                            } else {
                                viewModel.setAllItemsRead()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_done_all),
                            contentDescription = null
                        )
                    }
                },
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .nestedScroll(swipeState.nestedScrollConnection)
                ) {
                    when {
                        items.isLoading() -> {
                            Log.d("TAG", "loading")
                            CenteredColumn {
                                CircularProgressIndicator()
                            }
                        }

                        items.isError() -> Text(text = "error")
                        else -> {
                            LazyColumn(
                                state = scrollState,
                                contentPadding = PaddingValues(vertical = MaterialTheme.spacing.shortSpacing),
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.shortSpacing)
                            ) {
                                items(
                                    count = items.itemCount,
                                    //key = { items[it]!! },
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
                                        onShare = {
                                            viewModel.shareItem(itemWithFeed.item, context)
                                        },
                                        compactLayout = true
                                    )
                                }
                            }
                        }
                    }

                    PullToRefreshContainer(
                        state = swipeState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}


fun <T : Any> LazyPagingItems<T>.isLoading(): Boolean {
    return loadState.append is LoadState.Loading //|| loadState.refresh is LoadState.Loading
}

fun <T : Any> LazyPagingItems<T>.isError(): Boolean {
    return loadState.append is LoadState.Error //|| loadState.refresh is LoadState.Error
}