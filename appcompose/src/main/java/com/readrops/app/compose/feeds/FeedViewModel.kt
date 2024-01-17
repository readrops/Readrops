package com.readrops.app.compose.feeds

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.readrops.api.localfeed.LocalRSSDataSource
import com.readrops.api.utils.HtmlParser
import com.readrops.app.compose.base.TabViewModel
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
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

    init {
        viewModelScope.launch(context = Dispatchers.IO) {
            accountEvent.consumeAsFlow()
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
    }

    fun closeDialog() = _feedState.update { it.copy(dialog = null) }

    fun openDialog(state: DialogState) = _feedState.update { it.copy(dialog = state) }

    fun deleteFeed(feed: Feed) {
        viewModelScope.launch(Dispatchers.IO) {
            repository?.deleteFeed(feed)
        }
    }

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

        if (url.isEmpty()) {
            _addFeedDialogState.update {
                it.copy(
                    error = AddFeedDialogState.AddFeedError.EmptyUrl
                )
            }

            return
        } else if (!Patterns.WEB_URL.matcher(url).matches()) {
            _addFeedDialogState.update {
                it.copy(
                    error = AddFeedDialogState.AddFeedError.BadUrl
                )
            }

            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (localRSSDataSource.isUrlRSSResource(url)) {
                // TODO add support for all account types
                repository?.insertNewFeeds(listOf(url))

                _addFeedDialogState.update {
                    it.copy(closeDialog = true)
                }
            } else {
                val rssUrls = HtmlParser.getFeedLink(url, get())

                if (rssUrls.isEmpty()) {
                    _addFeedDialogState.update {
                        it.copy(
                            error = AddFeedDialogState.AddFeedError.NoRSSFeed
                        )
                    }
                } else {
                    // TODO add support for all account types
                    repository?.insertNewFeeds(rssUrls.map { it.url })

                    _addFeedDialogState.update {
                        it.copy(closeDialog = true)
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
                closeDialog = false
            )
        }
    }
}

