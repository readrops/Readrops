package com.readrops.app.feeds

import com.readrops.app.util.components.TextFieldError
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.AccountConfig

data class FeedState(
    val foldersAndFeeds: FolderAndFeedsState = FolderAndFeedsState.InitialState,
    val dialog: DialogState? = null,
    val areFoldersExpanded: Boolean = false,
    val error: String? = null,
    val config: AccountConfig? = null,
    val isAccountNotificationsEnabled: Boolean = false
) {

    val displayThreeDotsMenu
        get() = config?.canUpdateFolder == true && config.canDeleteFolder
}

sealed interface DialogState {
    data object AddFolder : DialogState
    class DeleteFeed(val feed: Feed) : DialogState
    class DeleteFolder(val folder: Folder) : DialogState
    class UpdateFeed(val feed: Feed, val folder: Folder?) : DialogState
    class UpdateFolder(val folder: Folder) : DialogState
    data class UpdateFeedOpenInSetting(val feed: Feed) : DialogState

    data class FeedSheet(
        val feed: Feed,
        val folder: Folder?,
        val config: AccountConfig
    ) : DialogState
}

sealed class FolderAndFeedsState {
    data object InitialState : FolderAndFeedsState()
    data class ErrorState(val exception: Exception) : FolderAndFeedsState()
    data class LoadedState(val values: Map<Folder?, List<Feed>>) : FolderAndFeedsState()
}

data class UpdateFeedDialogState(
    val feedId: Int = 0,
    val feedRemoteId: String? = null,
    val feedName: String = "",
    val feedNameError: TextFieldError? = null,
    val feedUrl: String = "",
    val feedUrlError: TextFieldError? = null,
    val selectedFolder: Folder? = null,
    val folders: List<Folder> = listOf(),
    val isFolderDropDownExpanded: Boolean = false,
    val isFeedUrlReadOnly: Boolean = true,
    val error: String? = null,
    val isLoading: Boolean = false
) {
    val isFeedNameError
        get() = feedNameError != null

    val isFeedUrlError
        get() = feedUrlError != null

    val hasFolders = folders.isNotEmpty()
}