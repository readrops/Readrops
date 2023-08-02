package com.readrops.app.compose.timelime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.readrops.app.compose.item.ItemScreen
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

    @Composable
    override fun Content() {
        val viewModel = getViewModel<TimelineViewModel>()
        val state by viewModel.timelineState.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()

        val swipeToRefreshState = rememberSwipeRefreshState(isRefreshing)

        val navigator = LocalNavigator.currentOrThrow

        SwipeRefresh(
            state = swipeToRefreshState,
            onRefresh = {
                viewModel.refreshTimeline()
            },
        ) {
            when (state) {
                is TimelineState.LoadedState -> {
                    val items = (state as TimelineState.LoadedState).items

                    if (items.isNotEmpty()) {
                        LazyColumn {
                            items(
                                    items = items
                            ) {
                                TimelineItem(
                                        onClick = { navigator.push(ItemScreen()) }
                                )
                            }
                        }
                    } else {
                        NoItemPlaceholder()
                    }
                }
                else -> {
                    NoItemPlaceholder()
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