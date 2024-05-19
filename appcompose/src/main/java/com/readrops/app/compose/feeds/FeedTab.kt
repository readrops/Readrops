package com.readrops.app.compose.feeds

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.readrops.app.compose.R
import com.readrops.app.compose.feeds.dialogs.AddFeedDialog
import com.readrops.app.compose.feeds.dialogs.FeedModalBottomSheet
import com.readrops.app.compose.feeds.dialogs.FolderDialog
import com.readrops.app.compose.feeds.dialogs.UpdateFeedDialog
import com.readrops.app.compose.util.components.CenteredProgressIndicator
import com.readrops.app.compose.util.components.ErrorMessage
import com.readrops.app.compose.util.components.Placeholder
import com.readrops.app.compose.util.components.TwoChoicesDialog
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.entities.Feed

object FeedTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = stringResource(R.string.feeds)
        )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val haptic = LocalHapticFeedback.current
        val uriHandler = LocalUriHandler.current

        val viewModel = getScreenModel<FeedScreenModel>()

        val state by viewModel.feedsState.collectAsStateWithLifecycle()

        when (val dialog = state.dialog) {
            is DialogState.AddFeed -> {
                AddFeedDialog(
                    viewModel = viewModel,
                    onDismiss = {
                        viewModel.closeDialog(DialogState.AddFeed)
                    },
                )
            }

            is DialogState.DeleteFeed -> {
                TwoChoicesDialog(
                    title = stringResource(R.string.delete_feed),
                    text = "Do you want to delete feed ${dialog.feed.name}?",
                    icon = rememberVectorPainter(image = Icons.Default.Delete),
                    confirmText = stringResource(R.string.delete),
                    dismissText = stringResource(R.string.cancel),
                    onDismiss = { viewModel.closeDialog() },
                    onConfirm = {
                        viewModel.deleteFeed(dialog.feed)
                        viewModel.closeDialog()
                    }
                )
            }

            is DialogState.FeedSheet -> {
                FeedModalBottomSheet(
                    feed = dialog.feed,
                    onDismissRequest = { viewModel.closeDialog() },
                    onOpen = {
                        uriHandler.openUri(dialog.feed.siteUrl!!)
                        viewModel.closeDialog()
                    },
                    onUpdate = {
                        viewModel.openDialog(DialogState.UpdateFeed(dialog.feed, dialog.folder))
                    },
                    onUpdateColor = {},
                    onDelete = { viewModel.openDialog(DialogState.DeleteFeed(dialog.feed)) }
                )
            }

            is DialogState.UpdateFeed -> {
                UpdateFeedDialog(
                    viewModel = viewModel,
                    onDismissRequest = { viewModel.closeDialog() }
                )
            }

            DialogState.AddFolder -> {
                FolderDialog(
                    viewModel = viewModel,
                    onDismiss = {
                        viewModel.closeDialog(DialogState.AddFolder)
                    },
                    onValidate = {
                        viewModel.folderValidate()
                    }
                )
            }

            is DialogState.DeleteFolder -> {
                TwoChoicesDialog(
                    title = stringResource(R.string.delete_folder),
                    text = "Do you want to delete folder ${dialog.folder.name}?",
                    icon = rememberVectorPainter(image = Icons.Default.Delete),
                    confirmText = stringResource(R.string.delete),
                    dismissText = stringResource(R.string.cancel),
                    onDismiss = { viewModel.closeDialog() },
                    onConfirm = {
                        viewModel.deleteFolder(dialog.folder)
                        viewModel.closeDialog()
                    }
                )
            }

            is DialogState.UpdateFolder -> {
                FolderDialog(
                    updateFolder = true,
                    viewModel = viewModel,
                    onDismiss = {
                        viewModel.closeDialog(DialogState.UpdateFolder(dialog.folder))
                    },
                    onValidate = {
                        viewModel.folderValidate(updateFolder = true)
                    }
                )
            }

            null -> {}
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.feeds)) },
                    actions = {
                        IconButton(
                            onClick = { viewModel.setFolderExpandState(state.areFoldersExpanded.not()) }
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (state.areFoldersExpanded)
                                        R.drawable.ic_unfold_less
                                    else
                                        R.drawable.ic_unfold_more
                                ),
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Column {
                    if (state.displayFolderCreationButton) {
                        SmallFloatingActionButton(
                            modifier = Modifier
                                .padding(
                                    start = MaterialTheme.spacing.veryShortSpacing,
                                    bottom = MaterialTheme.spacing.shortSpacing
                                ),
                            onClick = { viewModel.openDialog(DialogState.AddFolder) }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_new_folder),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = { viewModel.openDialog(DialogState.AddFeed) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (state.foldersAndFeeds) {
                    is FolderAndFeedsState.LoadedState -> {
                        val foldersAndFeeds =
                            (state.foldersAndFeeds as FolderAndFeedsState.LoadedState).values

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
                                        val folder = folderWithFeeds.first!!

                                        FolderExpandableItem(
                                            folder = folder,
                                            feeds = folderWithFeeds.second,
                                            isExpanded = state.areFoldersExpanded,
                                            onFeedClick = { feed ->
                                                viewModel.openDialog(
                                                    DialogState.FeedSheet(feed, folder)
                                                )
                                            },
                                            onFeedLongClick = { feed -> onFeedLongClick(feed) },
                                            onUpdateFolder = {
                                                viewModel.openDialog(
                                                    DialogState.UpdateFolder(folder)
                                                )
                                            },
                                            onDeleteFolder = {
                                                viewModel.openDialog(
                                                    DialogState.DeleteFolder(folder)
                                                )
                                            }
                                        )
                                    } else {
                                        val feeds = folderWithFeeds.second

                                        for (feed in feeds) {
                                            FeedItem(
                                                feed = feed,
                                                onClick = {
                                                    viewModel.openDialog(
                                                        DialogState.FeedSheet(feed, null)
                                                    )
                                                },
                                                onLongClick = { onFeedLongClick(feed) },
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Placeholder(
                                text = stringResource(R.string.no_feed),
                                painter = painterResource(R.drawable.ic_rss_feed_grey)
                            )
                        }
                    }

                    is FolderAndFeedsState.InitialState -> {
                        CenteredProgressIndicator()
                    }

                    is FolderAndFeedsState.ErrorState -> {
                        val exception =
                            (state.foldersAndFeeds as FolderAndFeedsState.ErrorState).exception
                        ErrorMessage(exception = exception)
                    }
                }
            }
        }
    }
}