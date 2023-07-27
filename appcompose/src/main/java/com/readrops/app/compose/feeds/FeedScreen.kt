package com.readrops.app.compose.feeds

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import org.koin.androidx.compose.getViewModel

class FeedScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel = getViewModel<FeedViewModel>()
        var showDialog by remember { mutableStateOf(false) }

        val state by viewModel.feedsState.collectAsState()

        if (showDialog) {
            AddFeedDialog(
                onDismiss = { showDialog = false },
                onValidate = {
                    showDialog = false
                    viewModel.insertFeed(it)
                }
            )
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (state) {
                is FeedsState.LoadedState -> {
                    val feeds = (state as FeedsState.LoadedState).feeds

                    if (feeds.isNotEmpty()) {
                        LazyColumn {
                            items(
                                    items = feeds
                            ) { feed ->
                                FeedItem(
                                        feed = feed,
                                )
                            }
                        }
                    }
                }
                is FeedsState.ErrorState -> {

                }
                else -> {

                }
            }



            FloatingActionButton(
                modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                onClick = { showDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }

    }
}