package com.readrops.app.compose.timelime

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            modifier = Modifier.fillMaxSize()
        ) {
            when (state) {
                is TimelineState.ErrorState -> {
                    Text(text = "error")
                }

                TimelineState.InitialState -> {}
                is TimelineState.LoadedState -> {
                    LazyColumn {
                        items(
                            items = (state as TimelineState.LoadedState).items
                        ) {
                            TimelineItem()
                        }
                    }
                }
            }
        }
    }
}