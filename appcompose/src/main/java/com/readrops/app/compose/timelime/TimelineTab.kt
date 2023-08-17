package com.readrops.app.compose.timelime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.readrops.app.compose.item.ItemScreen
import com.readrops.app.compose.util.theme.spacing
import org.koin.androidx.compose.getViewModel


object TimelineTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            return TabOptions(
                index = 1u,
                title = "Timeline",
            )
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = getViewModel<TimelineViewModel>()
        val state by viewModel.timelineState.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()

        val scrollState = rememberLazyListState()
        val swipeToRefreshState = rememberSwipeRefreshState(isRefreshing)

        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Timeline") }
                )
            }
        ) { paddingValues ->
            SwipeRefresh(
                state = swipeToRefreshState,
                onRefresh = {
                    viewModel.refreshTimeline()
                },
                modifier = Modifier.padding(paddingValues)
            ) {
                when (state) {
                    is TimelineState.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is TimelineState.Error -> {

                    }

                    is TimelineState.Loaded -> {
                        val items = (state as TimelineState.Loaded).items

                        if (items.isNotEmpty()) {
                            LazyColumn(
                                state = scrollState,
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.shortSpacing)
                            ) {
                                items(
                                    items = items,
                                    key = { it.item.id },
                                    contentType = { "item_with_feed" }
                                ) { itemWithFeed ->
                                    TimelineItem(
                                        itemWithFeed = itemWithFeed,
                                        onClick = { navigator.push(ItemScreen()) },
                                        onFavorite = {},
                                        onReadLater = {},
                                        onShare = {},
                                    )
                                }
                            }
                        } else {
                            NoItemPlaceholder()
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