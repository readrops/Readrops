package com.readrops.app.compose.feeds

import com.readrops.app.compose.util.components.TextFieldError
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType

data class FeedState(
    val foldersAndFeeds: FolderAndFeedsState = FolderAndFeedsState.InitialState,
    val dialog: DialogState? = null,
    val areFoldersExpanded: Boolean = false
)

sealed interface DialogState {
    object AddFeed : DialogState
    object AddFolder : DialogState
    class DeleteFeed(val feed: Feed) : DialogState
    class DeleteFolder(val folder: Folder) : DialogState
    class UpdateFeed(val feed: Feed, val folder: Folder?) : DialogState
    class UpdateFolder(val folder: Folder) : DialogState
    class FeedSheet(val feed: Feed, val folder: Folder?) : DialogState
}

sealed class FolderAndFeedsState {
    object InitialState : FolderAndFeedsState()
    data class ErrorState(val exception: Exception) : FolderAndFeedsState()
    data class LoadedState(val values: Map<Folder?, List<Feed>>) : FolderAndFeedsState()
}

data class AddFeedDialogState(
    val url: String = "",
    val selectedAccount: Account = Account(accountName = ""),
    val accounts: List<Account> = listOf(),
    val error: TextFieldError? = null,
) {
    val isError: Boolean get() = error != null
}

data class UpdateFeedDialogState(
    val feedId: Int = 0,
    val feedName: String = "",
    val feedNameError: TextFieldError? = null,
    val feedUrl: String = "",
    val feedUrlError: TextFieldError? = null,
    val accountType: AccountType? = null,
    val selectedFolder: Folder? = null,
    val folders: List<Folder> = listOf(),
    val isAccountDropDownExpanded: Boolean = false,
) {
    val isFeedNameError
        get() = feedNameError != null

    val isFeedUrlError
        get() = feedUrlError != null

    val isFeedUrlReadOnly: Boolean
        get() = accountType != null && !accountType.accountConfig!!.isFeedUrlEditable

    val hasFolders = folders.isNotEmpty()
}

data class FolderState(
    val folder: Folder = Folder(),
    val nameError: TextFieldError? = null,
) {
    val name = folder.name

    val isError = nameError != null
}