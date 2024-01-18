package com.readrops.app.compose.feeds

import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType

data class FeedState(
    val foldersAndFeeds: FolderAndFeedsState = FolderAndFeedsState.InitialState,
    val dialog: DialogState? = null,
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
    val error: AddFeedError? = null,
) {
    val isError: Boolean get() = error != null

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

data class UpdateFeedDialogState(
    val feedName: String = "",
    val feedNameError: Error? = null,
    val feedUrl: String = "",
    val feedUrlError: Error? = null,
    val accountType: AccountType? = null,
    val selectedFolder: Folder? = null,
    val folders: List<Folder> = listOf(),
    val isAccountDropDownExpanded: Boolean = false,
) {

    sealed class Error {
        object EmptyField : Error()
        object BadUrl : Error()
        object NoRSSUrl : Error()
    }

    val isFeedNameError
        get() = feedNameError != null

    val isFeedUrlError
        get() = feedUrlError != null

    fun errorText(error: Error?): String = when (error) {
        Error.BadUrl -> "Input is not a valid URL"
        Error.EmptyField -> "Field can't be empty"
        Error.NoRSSUrl -> "The provided URL is not a valid RSS feed"
        else -> ""
    }

    val isFeedUrlReadOnly: Boolean
        get() = accountType != null && !accountType.accountConfig!!.isFeedUrlEditable

}