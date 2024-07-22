package com.readrops.app.timelime

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.readrops.app.R
import com.readrops.app.item.ItemScreen
import com.readrops.app.timelime.drawer.TimelineDrawer
import com.readrops.app.util.ErrorMessage
import com.readrops.app.util.components.CenteredProgressIndicator
import com.readrops.app.util.components.Placeholder
import com.readrops.app.util.components.RefreshScreen
import com.readrops.app.util.components.dialog.TwoChoicesDialog
import com.readrops.app.util.theme.spacing
import com.readrops.db.filters.ListSortType
import com.readrops.db.filters.MainFilter
import com.readrops.db.filters.SubFilter
import com.readrops.db.pojo.ItemWithFeed
import kotlinx.coroutines.flow.filter


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

        val screenModel = getScreenModel<TimelineScreenModel>()
        val state by screenModel.timelineState.collectAsStateWithLifecycle()
        val items = state.itemState.collectAsLazyPagingItems()

        val lazyListState = rememberLazyListState()
        val pullToRefreshState = rememberPullToRefreshState()
        val snackbarHostState = remember { SnackbarHostState() }
        val topAppBarState = rememberTopAppBarState()
        val topAppBarScrollBehavior =
            TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

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
                screenModel.refreshTimeline(context)
            }
        }

        LaunchedEffect(state.scrollToTop) {
            if (state.scrollToTop) {
                lazyListState.scrollToItem(0)
                screenModel.resetScrollToTop()
                topAppBarState.contentOffset = 0f
            }
        }

        val drawerState = rememberDrawerState(
            initialValue = DrawerValue.Closed,
            confirmStateChange = {
                if (it == DrawerValue.Closed) {
                    screenModel.closeDrawer()
                } else {
                    screenModel.openDrawer()
                }

                true
            }
        )

        BackHandler(
            enabled = state.isDrawerOpen,
            onBack = { screenModel.closeDrawer() }
        )

        LaunchedEffect(state.isDrawerOpen) {
            if (state.isDrawerOpen) {
                drawerState.open()
            } else {
                drawerState.close()
            }
        }

        LaunchedEffect(state.localSyncErrors) {
            if (state.localSyncErrors != null) {
                val action = snackbarHostState.showSnackbar(
                    message = context.resources.getQuantityString(
                        R.plurals.error_occurred,
                        state.localSyncErrors!!.size
                    ),
                    actionLabel = context.getString(R.string.details),
                    duration = SnackbarDuration.Short
                )

                if (action == SnackbarResult.ActionPerformed) {
                    screenModel.openDialog(DialogState.ErrorList(state.localSyncErrors!!))
                } else {
                    // remove errors from state
                    screenModel.closeDialog(DialogState.ErrorList(state.localSyncErrors!!))
                }
            }
        }

        LaunchedEffect(state.syncError) {
            if (state.syncError != null) {
                snackbarHostState.showSnackbar(ErrorMessage.get(state.syncError!!, context))
                screenModel.resetSyncError()
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
                    onDismiss = { screenModel.closeDialog() },
                    onConfirm = {
                        screenModel.closeDialog()
                        screenModel.setAllItemsRead()
                    }
                )
            }

            is DialogState.FilterSheet -> {
                FilterBottomSheet(
                    filters = state.filters,
                    onSetShowReadItemsState = {
                        screenModel.setShowReadItemsState(!state.filters.showReadItems)
                    },
                    onSetSortTypeState = {
                        screenModel.setSortTypeState(
                            if (state.filters.sortType == ListSortType.NEWEST_TO_OLDEST)
                                ListSortType.OLDEST_TO_NEWEST
                            else
                                ListSortType.NEWEST_TO_OLDEST
                        )
                    },
                    onDismiss = { screenModel.closeDialog() }
                )
            }

            is DialogState.ErrorList -> {
                ErrorListDialog(
                    errorResult = dialog.errorResult,
                    onDismiss = { screenModel.closeDialog(dialog) }
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
                        screenModel.updateDrawerDefaultItem(it)
                    },
                    onFolderClick = {
                        screenModel.updateDrawerFolderSelection(it)
                    },
                    onFeedClick = {
                        screenModel.updateDrawerFeedSelection(it)
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
                                onClick = { screenModel.openDrawer() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { screenModel.openDialog(DialogState.FilterSheet) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_filter_list),
                                    contentDescription = null
                                )
                            }

                            IconButton(
                                onClick = { screenModel.refreshTimeline(context) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sync),
                                    contentDescription = null
                                )
                            }
                        },
                        scrollBehavior = topAppBarScrollBehavior
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    if (!state.hideReadAllFAB) {
                        FloatingActionButton(
                            onClick = {
                                if (state.filters.mainFilter == MainFilter.ALL) {
                                    screenModel.openDialog(DialogState.ConfirmDialog)
                                } else {
                                    screenModel.setAllItemsRead()
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_done_all),
                                contentDescription = null
                            )
                        }
                    }
                },
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .nestedScroll(pullToRefreshState.nestedScrollConnection)
                        .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
                ) {
                    when {
                        state.displayRefreshScreen -> RefreshScreen(
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
                                MarkItemsRead(
                                    lazyListState = lazyListState,
                                    items = items,
                                    markReadOnScroll = state.markReadOnScroll,
                                    screenModel = screenModel
                                )

                                LazyColumn(
                                    state = lazyListState,
                                    contentPadding = PaddingValues(
                                        vertical = if (state.itemSize == TimelineItemSize.COMPACT) {
                                            0.dp
                                        } else {
                                            MaterialTheme.spacing.shortSpacing
                                        }
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(
                                        if (state.itemSize == TimelineItemSize.COMPACT) {
                                            0.dp
                                        } else
                                            MaterialTheme.spacing.shortSpacing
                                    )
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
                                                    screenModel.setItemRead(itemWithFeed.item)
                                                    navigator.push(ItemScreen(itemWithFeed.item.id))
                                                },
                                                onFavorite = {
                                                    screenModel.updateStarState(itemWithFeed.item)
                                                },
                                                onShare = {
                                                    screenModel.shareItem(itemWithFeed.item, context)
                                                },
                                                size = state.itemSize
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

    @Composable
    private fun MarkItemsRead(
        lazyListState: LazyListState,
        items: LazyPagingItems<ItemWithFeed>,
        markReadOnScroll: Boolean,
        screenModel: TimelineScreenModel
    ) {
        val lastFirstVisibleItemIndex by screenModel.listIndexState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            snapshotFlow { lazyListState.firstVisibleItemIndex }
                .filter {
                    if (it < lastFirstVisibleItemIndex) {
                        screenModel.updateLastFirstVisibleItemIndex(it)
                    }

                    it > lastFirstVisibleItemIndex
                }
                .collect { newLastFirstVisibleItemIndex ->
                    if (newLastFirstVisibleItemIndex - lastFirstVisibleItemIndex > 1) {
                        val difference = newLastFirstVisibleItemIndex - lastFirstVisibleItemIndex

                        for (subCount in 0 until difference) {
                            val item = items[lastFirstVisibleItemIndex + subCount]?.item

                            if (item != null && !item.isRead && markReadOnScroll) {
                                screenModel.setItemRead(item)
                            }
                        }
                    } else {
                        val item = items[lastFirstVisibleItemIndex]?.item

                        if (item != null && !item.isRead && markReadOnScroll) {
                            screenModel.setItemRead(item)
                        }
                    }

                    screenModel.updateLastFirstVisibleItemIndex(newLastFirstVisibleItemIndex)
                }
        }
    }
}


fun <T : Any> LazyPagingItems<T>.isLoading(): Boolean {
    return loadState.refresh is LoadState.Loading && itemCount == 0
}

fun <T : Any> LazyPagingItems<T>.isError(): Boolean {
    return loadState.append is LoadState.Error //|| loadState.refresh is LoadState.Error
}