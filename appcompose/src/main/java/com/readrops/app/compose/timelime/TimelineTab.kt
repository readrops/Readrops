package com.readrops.app.compose.timelime

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
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
import com.readrops.app.compose.util.components.CenteredColumn
import com.readrops.app.compose.util.components.TwoChoicesDialog
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.filters.FilterType
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
        val items = state.itemState.collectAsLazyPagingItems()

        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        val scrollState = rememberLazyListState()

        // Use the depreciated refresh swipe as the material 3 one isn't available yet
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
                                text = when (state.filters.filterType) {
                                    FilterType.FEED_FILTER -> state.filterFeedName
                                    FilterType.FOLDER_FILER -> state.filterFolderName
                                    FilterType.READ_IT_LATER_FILTER -> stringResource(R.string.read_later)
                                    FilterType.STARS_FILTER -> stringResource(R.string.favorites)
                                    FilterType.NO_FILTER -> stringResource(R.string.articles)
                                    FilterType.NEW -> stringResource(R.string.new_articles)
                                }
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
                            if (state.filters.filterType == FilterType.NO_FILTER) {
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
                SwipeRefresh(
                    state = swipeState,
                    onRefresh = { viewModel.refreshTimeline() },
                    modifier = Modifier.padding(paddingValues)
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
                                        onReadLater = {},
                                        onShare = {
                                            viewModel.shareItem(itemWithFeed.item, context)
                                        },
                                        compactLayout = true
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


fun <T : Any> LazyPagingItems<T>.isLoading(): Boolean {
    return loadState.append is LoadState.Loading //|| loadState.refresh is LoadState.Loading
}

fun <T : Any> LazyPagingItems<T>.isError(): Boolean {
    return loadState.append is LoadState.Error //|| loadState.refresh is LoadState.Error
}