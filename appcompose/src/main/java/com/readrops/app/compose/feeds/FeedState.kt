package com.readrops.app.compose.feeds

import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account

data class FeedState(
    val foldersAndFeeds: FolderAndFeedsState = FolderAndFeedsState.InitialState,
    val dialog: DialogState? = null,
)

sealed interface DialogState {
    object AddFeed : DialogState
    object AddFolder : DialogState
    class DeleteFeed(val feed: Feed) : DialogState
    class DeleteFolder(val folder: Folder) : DialogState
    class UpdateFeed(val feed: Feed) : DialogState
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
    val error: AddFeedError? = null,
    val closeDialog: Boolean = false,
) {
    fun isError() = error != null

    val errorText: String
        get() = when (error) {
            is AddFeedError.EmptyUrl -> "Field can't be empty"
            AddFeedError.BadUrl -> "Input is not a valid URL"
            AddFeedError.NoConnection -> ""
            AddFeedError.NoRSSFeed -> "No RSS feed found"
            AddFeedError.UnreachableUrl -> ""
            else -> ""
        }

    sealed class AddFeedError {
        object EmptyUrl : AddFeedError()
        object BadUrl : AddFeedError()
        object UnreachableUrl : AddFeedError()
        object NoRSSFeed : AddFeedError()
        object NoConnection : AddFeedError()
    }
}