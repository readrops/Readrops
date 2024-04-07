package com.readrops.app.compose.timelime

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.paging.compose.itemKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.readrops.app.compose.R
import com.readrops.app.compose.item.ItemScreen
import com.readrops.app.compose.timelime.drawer.TimelineDrawer
import com.readrops.app.compose.util.components.CenteredProgressIndicator
import com.readrops.app.compose.util.components.Placeholder
import com.readrops.app.compose.util.components.RefreshScreen
import com.readrops.app.compose.util.components.TwoChoicesDialog
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.filters.ListSortType
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

        val viewModel = getScreenModel<TimelineScreenModel>()
        val state by viewModel.timelineState.collectAsStateWithLifecycle()
        val items = state.itemState.collectAsLazyPagingItems()

        val lazyListState = rememberLazyListState()
        val pullToRefreshState = rememberPullToRefreshState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(state.isRefreshing) {
            if (state.isRefreshing) {
                pullToRefreshState.startRefresh()
            } else {
                pullToRefreshState.endRefresh()
            }
        }

        // Material3 pull to refresh doesn't have a onRefresh callback,
        // so we need to listen to the internal state change to trigger the refresh
        LaunchedEffect(pullToRefreshState.isRefreshing) {
            if (pullToRefreshState.isRefreshing && !state.isRefreshing) {
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

        LaunchedEffect(state.synchronizationErrors) {
            if (state.synchronizationErrors != null) {
                val action = snackbarHostState.showSnackbar(
                    message = context.resources.getQuantityString(
                        R.plurals.error_occurred,
                        state.synchronizationErrors!!.size
                    ),
                    actionLabel = context.getString(R.string.details),
                    duration = SnackbarDuration.Short
                )

                if (action == SnackbarResult.ActionPerformed) {
                    viewModel.openDialog(DialogState.ErrorList(state.synchronizationErrors!!))
                } else {
                    // remove errors from state
                    viewModel.closeDialog(DialogState.ErrorList(state.synchronizationErrors!!))
                }
            }
        }

        when (val dialog = state.dialog) {
            is DialogState.ConfirmDialog -> {
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

            is DialogState.FilterSheet -> {
                FilterBottomSheet(
                    filters = state.filters,
                    onSetShowReadItemsState = {
                        viewModel.setShowReadItemsState(!state.filters.showReadItems)
                    },
                    onSetSortTypeState = {
                        viewModel.setSortTypeState(
                            if (state.filters.sortType == ListSortType.NEWEST_TO_OLDEST)
                                ListSortType.OLDEST_TO_NEWEST
                            else
                                ListSortType.NEWEST_TO_OLDEST
                        )
                    },
                    onDismiss = { viewModel.closeDialog() }
                )
            }

            is DialogState.ErrorList -> {
                ErrorListDialog(
                    errorResult = dialog.errorResult,
                    onDismiss = { viewModel.closeDialog(dialog) }
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
                            Column {
                                Text(
                                    text = when (state.filters.mainFilter) {
                                        MainFilter.STARS -> stringResource(R.string.favorites)
                                        MainFilter.ALL -> stringResource(R.string.articles)
                                        MainFilter.NEW -> stringResource(R.string.new_articles)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (state.showSubtitle) {
                                    Text(
                                        text = when (state.filters.subFilter) {
                                            SubFilter.FEED -> state.filterFeedName
                                            SubFilter.FOLDER -> state.filterFolderName
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
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
                snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        .fillMaxSize()
                        .nestedScroll(pullToRefreshState.nestedScrollConnection)
                ) {
                    when {
                        state.isRefreshing -> RefreshScreen(
                            currentFeed = state.currentFeed,
                            feedCount = state.feedCount,
                            feedMax = state.feedMax
                        )

                        items.isLoading() -> {
                            CenteredProgressIndicator()
                        }

                        items.isError() -> {
                            Placeholder(
                                text = stringResource(R.string.error_occured),
                                painter = painterResource(id = R.drawable.ic_error)
                            )
                        }

                        else -> {
                            if (items.itemCount > 0) {
                                LazyColumn(
                                    state = lazyListState,
                                    contentPadding = PaddingValues(vertical = MaterialTheme.spacing.shortSpacing),
                                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.shortSpacing)
                                ) {
                                    items(
                                        count = items.itemCount,
                                        key = items.itemKey { it.item.id },
                                    ) { itemCount ->
                                        val itemWithFeed = items[itemCount]

                                        if (itemWithFeed != null) {
                                            TimelineItem(
                                                itemWithFeed = itemWithFeed,
                                                onClick = {
                                                    viewModel.setItemRead(itemWithFeed.item)
                                                    navigator.push(ItemScreen(itemWithFeed.item.id))
                                                },
                                                onFavorite = {
                                                    viewModel.updateStarState(itemWithFeed.item)
                                                },
                                                onShare = {
                                                    viewModel.shareItem(itemWithFeed.item, context)
                                                },
                                                compactLayout = true
                                            )
                                        }
                                    }
                                }

                                PullToRefreshContainer(
                                    state = pullToRefreshState,
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )
                            } else {
                                // Empty lazyColumn to let the pull to refresh be usable
                                // when the no item placeholder is displayed
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {}

                                PullToRefreshContainer(
                                    state = pullToRefreshState,
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )

                                Placeholder(
                                    text = stringResource(R.string.no_item),
                                    painter = painterResource(R.drawable.ic_rss_feed_grey)
                                )
                            }
                        }
                    }
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