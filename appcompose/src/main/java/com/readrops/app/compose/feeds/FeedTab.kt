package com.readrops.app.compose.feeds

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.readrops.app.compose.R
import com.readrops.app.compose.util.components.Placeholder
import com.readrops.db.entities.Feed
import org.koin.androidx.compose.getViewModel

object FeedTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = "Feeds"
        )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val haptic = LocalHapticFeedback.current
        val uriHandler = LocalUriHandler.current
        val viewModel = getViewModel<FeedViewModel>()

        val state by viewModel.feedsState.collectAsStateWithLifecycle()

        when (val dialog = state.dialog) {
            is DialogState.AddFeed -> {
                AddFeedDialog(
                    viewModel = viewModel,
                    onDismiss = {
                        viewModel.closeDialog()
                        viewModel.resetAddFeedDialogState()
                    },
                )
            }
            is DialogState.DeleteFeed -> {}
            is DialogState.FeedSheet -> {
                FeedModalBottomSheet(
                    feed = dialog.feed,
                    folder = dialog.folder,
                    onDismissRequest = { viewModel.closeDialog() },
                    onOpen = { uriHandler.openUri(dialog.feed.siteUrl!!) },
                    onModify = { },
                    onUpdateColor = {},
                    onDelete = {},
                )
            }
            is DialogState.UpdateFeed -> {}
            null -> {}
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Feeds")
                    },
                    actions = {
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (state.foldersAndFeeds) {
                    is FolderAndFeedsState.LoadedState -> {
                        val foldersAndFeeds = (state.foldersAndFeeds as FolderAndFeedsState.LoadedState).values

                        if (foldersAndFeeds.isNotEmpty()) {
                            LazyColumn {
                                items(
                                    items = foldersAndFeeds.toList()
                                ) { folderWithFeeds ->

                                    fun onFeedLongClick(feed: Feed) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        uriHandler.openUri(feed.siteUrl!!)
                                    }

                                    if (folderWithFeeds.first != null) {
                                        FolderExpandableItem(
                                            folder = folderWithFeeds.first!!,
                                            feeds = folderWithFeeds.second,
                                            onFeedClick = { feed -> viewModel.openFeedSheet(feed, folderWithFeeds.first) },
                                            onFeedLongClick = { feed -> onFeedLongClick(feed) }
                                        )
                                    } else {
                                        val feeds = folderWithFeeds.second

                                        for (feed in feeds) {
                                            FeedItem(
                                                feed = feed,
                                                onClick = { viewModel.openFeedSheet(feed, null) },
                                                onLongClick = { onFeedLongClick(feed) },
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Placeholder(
                                text = "No feed",
                                painter = painterResource(R.drawable.ic_rss_feed_grey)
                            )
                        }
                    }

                    is FolderAndFeedsState.ErrorState -> {

                    }

                    else -> {

                    }
                }

                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    onClick = { viewModel.openAddFeedDialog() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        }
    }
}