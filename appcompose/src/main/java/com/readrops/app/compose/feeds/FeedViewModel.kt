package com.readrops.app.compose.feeds

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.readrops.api.localfeed.LocalRSSDataSource
import com.readrops.api.utils.HtmlParser
import com.readrops.app.compose.base.TabViewModel
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class FeedViewModel(
    database: Database,
    private val getFoldersWithFeeds: GetFoldersWithFeeds,
    private val localRSSDataSource: LocalRSSDataSource,
) : TabViewModel(database), KoinComponent {

    private val _feedState = MutableStateFlow(FeedState())
    val feedsState = _feedState.asStateFlow()

    private val _addFeedDialogState = MutableStateFlow(AddFeedDialogState())
    val addFeedDialogState = _addFeedDialogState.asStateFlow()

    private val _updateFeedDialogState = MutableStateFlow(UpdateFeedDialogState())
    val updateFeedDialogState = _updateFeedDialogState.asStateFlow()

    private val _addFolderState = MutableStateFlow(AddFolderState())
    val addFolderState = _addFolderState.asStateFlow()

    init {
        viewModelScope.launch(context = Dispatchers.IO) {
            accountEvent
                .flatMapConcat { account ->
                    getFoldersWithFeeds.get(account.id)
                }
                .catch { throwable ->
                    _feedState.update {
                        it.copy(foldersAndFeeds = FolderAndFeedsState.ErrorState(Exception(throwable)))
                    }
                }
                .collect { foldersAndFeeds ->
                    _feedState.update {
                        it.copy(foldersAndFeeds = FolderAndFeedsState.LoadedState(foldersAndFeeds))
                    }
                }
        }

        viewModelScope.launch(context = Dispatchers.IO) {
            database.newAccountDao()
                .selectAllAccounts()
                .collect { accounts ->
                    _addFeedDialogState.update { dialogState ->
                        dialogState.copy(
                            accounts = accounts,
                            selectedAccount = accounts.find { it.isCurrentAccount }!!
                        )
                    }
                }
        }

        viewModelScope.launch(context = Dispatchers.IO) {
            accountEvent
                .flatMapConcat { account ->
                    database.newFolderDao()
                        .selectFolders(account.id)
                }
                .collect { folders ->
                    _updateFeedDialogState.update {
                        it.copy(
                            folders = folders,
                            accountType = currentAccount!!.accountType
                        )
                    }
                }
        }
    }

    fun closeDialog() = _feedState.update { it.copy(dialog = null) }

    fun openDialog(state: DialogState) {
        if (state is DialogState.UpdateFeed) {
            _updateFeedDialogState.update {
                it.copy(
                    feedName = state.feed.name!!,
                    feedUrl = state.feed.url!!,
                    selectedFolder = state.folder
                )
            }
        }

        _feedState.update { it.copy(dialog = state) }
    }

    fun deleteFeed(feed: Feed) {
        viewModelScope.launch(Dispatchers.IO) {
            repository?.deleteFeed(feed)
        }
    }

    // Add feed

    fun setAddFeedDialogURL(url: String) {
        _addFeedDialogState.update {
            it.copy(
                url = url,
                error = null,
            )
        }
    }

    fun setAddFeedDialogSelectedAccount(account: Account) {
        _addFeedDialogState.update {
            it.copy(
                selectedAccount = account
            )
        }
    }

    fun addFeedDialogValidate() {
        val url = _addFeedDialogState.value.url

        when {
            url.isEmpty() -> {
                _addFeedDialogState.update {
                    it.copy(error = AddFeedDialogState.AddFeedError.EmptyUrl)
                }

                return
            }

            !Patterns.WEB_URL.matcher(url).matches() -> {
                _addFeedDialogState.update {
                    it.copy(error = AddFeedDialogState.AddFeedError.BadUrl)
                }

                return
            }

            else -> viewModelScope.launch(Dispatchers.IO) {
                if (localRSSDataSource.isUrlRSSResource(url)) {
                    // TODO add support for all account types
                    repository?.insertNewFeeds(listOf(url))

                    closeDialog()
                } else {
                    val rssUrls = HtmlParser.getFeedLink(url, get())

                    if (rssUrls.isEmpty()) {
                        _addFeedDialogState.update {
                            it.copy(error = AddFeedDialogState.AddFeedError.NoRSSFeed)
                        }
                    } else {
                        // TODO add support for all account types
                        repository?.insertNewFeeds(rssUrls.map { it.url })

                        closeDialog()
                    }
                }
            }
        }
    }

    fun resetAddFeedDialogState() {
        _addFeedDialogState.update {
            it.copy(
                url = "",
                error = null,
            )
        }
    }

    // add feed

    // update feed

    fun setAccountDropDownState(isExpanded: Boolean) {
        _updateFeedDialogState.update {
            it.copy(isAccountDropDownExpanded = isExpanded)
        }
    }

    fun setSelectedFolder(folder: Folder) {
        _updateFeedDialogState.update {
            it.copy(selectedFolder = folder)
        }
    }

    fun setUpdateFeedDialogStateFeedName(feedName: String) {
        _updateFeedDialogState.update {
            it.copy(
                feedName = feedName,
                feedNameError = null,
            )
        }
    }

    fun setUpdateFeedDialogFeedUrl(feedUrl: String) {
        _updateFeedDialogState.update {
            it.copy(
                feedUrl = feedUrl,
                feedUrlError = null,
            )
        }
    }

    fun updateFeedDialogValidate() {
        val feedName = _updateFeedDialogState.value.feedName
        val feedUrl = _updateFeedDialogState.value.feedUrl

        when {
            feedName.isEmpty() -> {
                _updateFeedDialogState.update {
                    it.copy(feedNameError = UpdateFeedDialogState.Error.EmptyField)
                }
                return
            }

            feedUrl.isEmpty() -> {
                _updateFeedDialogState.update {
                    it.copy(feedUrlError = UpdateFeedDialogState.Error.EmptyField)
                }
                return
            }

            !Patterns.WEB_URL.matcher(feedUrl).matches() -> {
                _updateFeedDialogState.update {
                    it.copy(feedUrlError = UpdateFeedDialogState.Error.BadUrl)
                }
                return
            }

            else -> {
                viewModelScope.launch(Dispatchers.IO) {
                    // TODO add logig to update feed
                    //repository?.updateFeed()
                    closeDialog()
                }
            }
        }
    }

    // update feed

    // add folder

    fun setFolderName(name: String) = _addFolderState.update {
        it.copy(
            name = name,
            isEmpty = false
        )
    }

    fun addFolderValidate() {
        val name = _addFolderState.value.name

        if (name.isEmpty()) {
            _addFolderState.update {
                it.copy(isEmpty = true)
            }

            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository?.addFolder(Folder(name = name, accountId = currentAccount?.id!!))

            closeDialog()
            resetAddFolderState()
        }
    }

    fun resetAddFolderState() {
        _addFolderState.update {
            it.copy(
                name = "",
                isEmpty = false
            )
        }
    }

    // add folder
}

