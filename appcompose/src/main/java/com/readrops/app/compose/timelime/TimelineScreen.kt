package com.readrops.app.compose.timelime

import androidx.compose.foundation.gestures.scrollable
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
import cafe.adriel.voyager.androidx.AndroidScreen
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.koin.androidx.compose.getViewModel


class TimelineScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel = getViewModel<TimelineViewModel>()
        val state by viewModel.timelineState.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()

        val swipeToRefreshState = rememberSwipeRefreshState(isRefreshing)

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
                                    items = (state as TimelineState.LoadedState).items
                            ) {
                                TimelineItem()
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
            modifier = Modifier.fillMaxSize()
                    .verticalScroll(scrollState)
    ) {
        Text(
                text = "No item",
                style = MaterialTheme.typography.displayMedium
        )
    }
}