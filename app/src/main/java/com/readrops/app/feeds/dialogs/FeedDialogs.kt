package com.readrops.app.feeds.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.R
import com.readrops.app.feeds.DialogState
import com.readrops.app.feeds.FeedScreenModel
import com.readrops.app.feeds.FeedState
import com.readrops.app.feeds.color.FeedColorScreen
import com.readrops.app.more.preferences.components.RadioButtonPreferenceDialog
import com.readrops.app.more.preferences.components.ToggleableInfo
import com.readrops.app.util.components.dialog.TextFieldDialog
import com.readrops.app.util.components.dialog.TwoChoicesDialog
import com.readrops.db.entities.OpenIn

@Composable
fun FeedDialogs(state: FeedState, screenModel: FeedScreenModel) {
    val uriHandler = LocalUriHandler.current
    val navigator = LocalNavigator.currentOrThrow

    val folderState by screenModel.folderState.collectAsStateWithLifecycle()

    when (val dialog = state.dialog) {
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
                accountNotificationsEnabled = state.isAccountNotificationsEnabled,
                onDismissRequest = { screenModel.closeDialog() },
                onOpen = {
                    uriHandler.openUri(dialog.feed.siteUrl!!)
                    screenModel.closeDialog()
                },
                onUpdate = {
                    screenModel.openDialog(DialogState.UpdateFeed(dialog.feed, dialog.folder))
                },
                onDelete = { screenModel.openDialog(DialogState.DeleteFeed(dialog.feed)) },
                onUpdateNotifications = {
                    screenModel.updateFeedNotifications(dialog.feed.id, it)
                },
                onOpenInClick = {
                    screenModel.openDialog(DialogState.UpdateFeedOpenInSetting(dialog.feed))
                },
                onUpdateColor = {
                    navigator.push(FeedColorScreen(dialog.feed))
                    screenModel.closeDialog(dialog)
                },
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
                text = if (state.config?.showCustomFolderDeleteMessage == true) {
                    stringResource(R.string.freshrss_delete_folder_question, dialog.folder.name!!)
                } else {
                    stringResource(R.string.delete_folder_question, dialog.folder.name!!)
                },
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

        is DialogState.UpdateFeedOpenInSetting -> {
            RadioButtonPreferenceDialog(
                title = stringResource(R.string.open_feed_in),
                entries = listOf(
                    ToggleableInfo(
                        key = OpenIn.LOCAL_VIEW,
                        text = stringResource(R.string.local_view),
                        isSelected = dialog.feed.openIn == OpenIn.LOCAL_VIEW
                    ),
                    ToggleableInfo(
                        key = OpenIn.EXTERNAL_VIEW,
                        text = stringResource(R.string.external_view),
                        isSelected = dialog.feed.openIn == OpenIn.EXTERNAL_VIEW
                    )
                ),
                onCheckChange = { screenModel.updateFeedOpenInSetting(dialog.feed.id, it) },
                onDismiss = { screenModel.closeDialog(dialog) },
            )
        }

        null -> {}
    }
}
