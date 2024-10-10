package com.readrops.app.feeds

import android.util.Patterns
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.readrops.app.R
import com.readrops.app.feeds.dialogs.AddFeedDialog
import com.readrops.app.feeds.dialogs.FeedModalBottomSheet
import com.readrops.app.feeds.dialogs.UpdateFeedDialog
import com.readrops.app.util.ErrorMessage
import com.readrops.app.util.components.CenteredProgressIndicator
import com.readrops.app.util.components.ErrorMessage
import com.readrops.app.util.components.Placeholder
import com.readrops.app.util.components.dialog.TextFieldDialog
import com.readrops.app.util.components.dialog.TwoChoicesDialog
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object FeedTab : Tab {

    private val addFeedDialogChannel = Channel<String>()

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
        val context = LocalContext.current

        val screenModel = koinScreenModel<FeedScreenModel>()
        val state by screenModel.feedsState.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }
        val topAppBarScrollBehavior =
            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        LaunchedEffect(state.exception) {
            if (state.exception != null) {
                snackbarHostState.showSnackbar(ErrorMessage.get(state.exception!!, context))
                screenModel.resetException()
            }
        }

        LaunchedEffect(Unit) {
            addFeedDialogChannel.receiveAsFlow()
                .collect { url ->
                    if (Patterns.WEB_URL.matcher(url).matches()) {
                        screenModel.openDialog(DialogState.AddFeed(url))
                    }
                }
        }

        FeedDialogs(
            state = state,
            screenModel = screenModel
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.feeds)) },
                    actions = {
                        IconButton(
                            onClick = { screenModel.setFolderExpandState(state.areFoldersExpanded.not()) }
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
                    },
                    scrollBehavior = topAppBarScrollBehavior
                )
            },
            floatingActionButton = {
                Column {
                    if (state.config?.canCreateFolder == true) {
                        SmallFloatingActionButton(
                            modifier = Modifier
                                .padding(
                                    start = MaterialTheme.spacing.veryShortSpacing,
                                    bottom = MaterialTheme.spacing.shortSpacing
                                ),
                            onClick = { screenModel.openDialog(DialogState.AddFolder) }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_new_folder),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (state.config?.canCreateFeed == true) {
                        FloatingActionButton(
                            onClick = { screenModel.openDialog(DialogState.AddFeed()) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
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
                                                screenModel.openDialog(
                                                    DialogState.FeedSheet(
                                                        feed = feed,
                                                        folder = folder,
                                                        config = state.config!!
                                                    )
                                                )
                                            },
                                            onFeedLongClick = { feed -> onFeedLongClick(feed) },
                                            onUpdateFolder = {
                                                screenModel.openDialog(
                                                    DialogState.UpdateFolder(folder)
                                                )
                                            },
                                            onDeleteFolder = {
                                                screenModel.openDialog(
                                                    DialogState.DeleteFolder(folder)
                                                )
                                            },
                                            displayThreeDotsMenu = state.displayThreeDotsMenu
                                        )
                                    } else {
                                        val feeds = folderWithFeeds.second

                                        for (feed in feeds) {
                                            FeedItem(
                                                feed = feed,
                                                onClick = {
                                                    screenModel.openDialog(
                                                        DialogState.FeedSheet(
                                                            feed = feed,
                                                            folder = null,
                                                            config = state.config!!
                                                        )
                                                    )
                                                },
                                                onLongClick = { onFeedLongClick(feed) },
                                                displayDivider = false
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

    @Composable
    private fun FeedDialogs(state: FeedState, screenModel: FeedScreenModel) {
        val uriHandler = LocalUriHandler.current

        val addFeedDialogState by screenModel.addFeedDialogState.collectAsStateWithLifecycle()
        val folderState by screenModel.folderState.collectAsStateWithLifecycle()

        when (val dialog = state.dialog) {
            is DialogState.AddFeed -> {
                AddFeedDialog(
                    state = addFeedDialogState,
                    onValueChange = { screenModel.setAddFeedDialogURL(it) },
                    onExpandChange = { screenModel.setAccountDropDownExpanded(it) },
                    onAccountClick = { screenModel.setAddFeedDialogSelectedAccount(it) },
                    onValidate = { screenModel.addFeedDialogValidate() },
                    onDismiss = { screenModel.closeDialog(DialogState.AddFeed()) },
                )
            }

            is DialogState.DeleteFeed -> {
                TwoChoicesDialog(
                    title = stringResource(R.string.delete_feed),
                    text = stringResource(R.string.delete_feed_question, dialog.feed.name!!),
                    icon = rememberVectorPainter(image = Icons.Default.Delete),
                    confirmText = stringResource(R.string.delete),
                    dismissText = stringResource(R.string.cancel),
                    onDismiss = { screenModel.closeDialog() },
                    onConfirm = {
                        screenModel.deleteFeed(dialog.feed)
                        screenModel.closeDialog()
                    }
                )
            }

            is DialogState.FeedSheet -> {
                FeedModalBottomSheet(
                    feed = dialog.feed,
                    onDismissRequest = { screenModel.closeDialog() },
                    onOpen = {
                        uriHandler.openUri(dialog.feed.siteUrl!!)
                        screenModel.closeDialog()
                    },
                    onUpdate = {
                        screenModel.openDialog(DialogState.UpdateFeed(dialog.feed, dialog.folder))
                    },
                    onDelete = { screenModel.openDialog(DialogState.DeleteFeed(dialog.feed)) },
                    canUpdateFeed = dialog.config.canUpdateFeed,
                    canDeleteFeed = dialog.config.canDeleteFeed
                )
            }

            is DialogState.UpdateFeed -> {
                UpdateFeedDialog(
                    viewModel = screenModel,
                    onDismissRequest = { screenModel.closeDialog(dialog) }
                )
            }

            DialogState.AddFolder -> {
                TextFieldDialog(
                    title = stringResource(id = R.string.add_folder),
                    icon = painterResource(id = R.drawable.ic_new_folder),
                    label = stringResource(id = R.string.name),
                    state = folderState,
                    onValueChange = { screenModel.setFolderName(it) },
                    onValidate = { screenModel.folderValidate() },
                    onDismiss = { screenModel.closeDialog(DialogState.AddFolder) }
                )
            }

            is DialogState.DeleteFolder -> {
                TwoChoicesDialog(
                    title = stringResource(R.string.delete_folder),
                    text = stringResource(R.string.delete_folder_question, dialog.folder.name!!),
                    icon = rememberVectorPainter(image = Icons.Default.Delete),
                    confirmText = stringResource(R.string.delete),
                    dismissText = stringResource(R.string.cancel),
                    onDismiss = { screenModel.closeDialog() },
                    onConfirm = {
                        screenModel.deleteFolder(dialog.folder)
                        screenModel.closeDialog()
                    }
                )
            }

            is DialogState.UpdateFolder -> {
                TextFieldDialog(
                    title = stringResource(id = R.string.edit_folder),
                    icon = painterResource(id = R.drawable.ic_folder_grey),
                    label = stringResource(id = R.string.name),
                    state = folderState,
                    onValueChange = { screenModel.setFolderName(it) },
                    onValidate = { screenModel.folderValidate(updateFolder = true) },
                    onDismiss = { screenModel.closeDialog(DialogState.UpdateFolder(dialog.folder)) }
                )
            }

            null -> {}
        }
    }

    suspend fun openAddFeedDialog(url: String) {
        addFeedDialogChannel.send(url)
    }
}