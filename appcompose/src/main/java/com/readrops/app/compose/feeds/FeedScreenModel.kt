package com.readrops.app.compose.feeds

import android.content.Context
import android.util.Patterns
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.api.localfeed.LocalRSSDataSource
import com.readrops.api.utils.HtmlParser
import com.readrops.app.compose.R
import com.readrops.app.compose.base.TabScreenModel
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.app.compose.util.components.TextFieldError
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.filters.MainFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class FeedScreenModel(
    database: Database,
    private val getFoldersWithFeeds: GetFoldersWithFeeds,
    private val localRSSDataSource: LocalRSSDataSource,
    private val context: Context
) : TabScreenModel(database), KoinComponent {

    private val _feedState = MutableStateFlow(FeedState())
    val feedsState = _feedState.asStateFlow()

    private val _addFeedDialogState = MutableStateFlow(AddFeedDialogState())
    val addFeedDialogState = _addFeedDialogState.asStateFlow()

    private val _updateFeedDialogState = MutableStateFlow(UpdateFeedDialogState())
    val updateFeedDialogState = _updateFeedDialogState.asStateFlow()

    private val _folderState = MutableStateFlow(FolderState())
    val folderState = _folderState.asStateFlow()

    init {
        screenModelScope.launch(context = Dispatchers.IO) {
            accountEvent
                .flatMapConcat { account ->
                    _feedState.update { it.copy(displayFolderCreationButton = account.config.canCreateFolder) }
                    _updateFeedDialogState.update {
                        it.copy(
                            isFeedUrlReadOnly = account.config.isFeedUrlReadOnly,
                            isNoFolderCase = account.config.isNoFolderCase
                        )
                    }

                    getFoldersWithFeeds.get(account.id, MainFilter.ALL)
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

        screenModelScope.launch(context = Dispatchers.IO) {
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

        screenModelScope.launch(context = Dispatchers.IO) {
            accountEvent
                .flatMapConcat { account ->
                    _updateFeedDialogState.update {
                        it.copy(
                            isFeedUrlReadOnly = account.config.isFeedUrlReadOnly,
                            isNoFolderCase = account.config.isNoFolderCase
                        )
                    }

                    database.newFolderDao()
                        .selectFolders(account.id)
                }
                .collect { folders ->
                    _updateFeedDialogState.update {
                        it.copy(
                            folders = if (!it.isNoFolderCase) { // TODO implement no folder case properly
                                folders + listOf(
                                    Folder(
                                        id = 0,
                                        name = context.resources.getString(R.string.no_folder)
                                    )
                                )
                            } else {
                                folders
                            }
                        )
                    }
                }
        }
    }

    fun setFolderExpandState(isExpanded: Boolean) =
        _feedState.update { it.copy(areFoldersExpanded = isExpanded) }

    fun closeDialog(dialog: DialogState? = null) {
        if (dialog is DialogState.AddFeed) {
            _addFeedDialogState.update {
                it.copy(
                    url = "",
                    error = null,
                )
            }
        } else if (dialog is DialogState.AddFolder || dialog is DialogState.UpdateFolder) {
            _folderState.update {
                it.copy(
                    folder = Folder(),
                    nameError = null,
                    exception = null
                )
            }
        }

        _feedState.update { it.copy(dialog = null) }
    }

    fun openDialog(state: DialogState) {
        if (state is DialogState.UpdateFeed) {
            _updateFeedDialogState.update {
                it.copy(
                    feedId = state.feed.id,
                    feedName = state.feed.name!!,
                    feedUrl = state.feed.url!!,
                    selectedFolder = state.folder
                )
            }
        }

        if (state is DialogState.UpdateFolder) {
            _folderState.update {
                it.copy(
                    folder = state.folder
                )
            }
        }

        _feedState.update { it.copy(dialog = state) }
    }

    fun deleteFeed(feed: Feed) {
        screenModelScope.launch(Dispatchers.IO) {
            repository?.deleteFeed(feed)
        }
    }

    fun deleteFolder(folder: Folder) {
        screenModelScope.launch(Dispatchers.IO) {
            try {
                repository?.deleteFolder(folder)
            } catch (e: Exception) {
                _feedState.update { it.copy(exception = e) }
            }
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
        _addFeedDialogState.update { it.copy(selectedAccount = account) }
    }

    fun addFeedDialogValidate() {
        val url = _addFeedDialogState.value.url

        when {
            url.isEmpty() -> {
                _addFeedDialogState.update {
                    it.copy(error = TextFieldError.EmptyField)
                }

                return
            }

            !Patterns.WEB_URL.matcher(url).matches() -> {
                _addFeedDialogState.update {
                    it.copy(error = TextFieldError.BadUrl)
                }

                return
            }

            else -> screenModelScope.launch(Dispatchers.IO) {
                try {
                    if (localRSSDataSource.isUrlRSSResource(url)) {
                        // TODO add support for all account types
                        repository?.insertNewFeeds(
                            newFeeds = listOf(Feed(url = url))
                        ) {}

                        closeDialog(DialogState.AddFeed)
                    } else {
                        val rssUrls = HtmlParser.getFeedLink(url, get())

                        if (rssUrls.isEmpty()) {
                            _addFeedDialogState.update {
                                it.copy(error = TextFieldError.NoRSSFeed)
                            }
                        } else {
                            // TODO add support for all account types
                            repository?.insertNewFeeds(
                                newFeeds = rssUrls.map { Feed(url = it.url) }
                            ) {}

                            closeDialog(DialogState.AddFeed)
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is UnknownHostException -> _addFeedDialogState.update { it.copy(error = TextFieldError.UnreachableUrl) }
                        else -> _addFeedDialogState.update { it.copy(error = TextFieldError.NoRSSFeed) }
                    }
                }
            }
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
                    it.copy(feedNameError = TextFieldError.EmptyField)
                }
                return
            }

            feedUrl.isEmpty() -> {
                _updateFeedDialogState.update {
                    it.copy(feedUrlError = TextFieldError.EmptyField)
                }
                return
            }

            !Patterns.WEB_URL.matcher(feedUrl).matches() -> {
                _updateFeedDialogState.update {
                    it.copy(feedUrlError = TextFieldError.BadUrl)
                }
                return
            }

            else -> {
                screenModelScope.launch(Dispatchers.IO) {
                    with(_updateFeedDialogState.value) {
                        repository?.updateFeed(
                            Feed(
                                id = feedId,
                                name = feedName,
                                url = feedUrl,
                                folderId = selectedFolder?.id
                            )
                        )
                    }

                    closeDialog()
                }
            }
        }
    }

    // update feed

    // add/update folder

    fun setFolderName(name: String) = _folderState.update {
        it.copy(
            folder = it.folder.copy(name = name),
            nameError = null,
        )
    }

    fun folderValidate(updateFolder: Boolean = false) {
        val name = _folderState.value.name.orEmpty()

        if (name.isEmpty()) {
            _folderState.update { it.copy(nameError = TextFieldError.EmptyField) }

            return
        }

        screenModelScope.launch(Dispatchers.IO) {
            try {
                if (updateFolder) {
                    repository?.updateFolder(_folderState.value.folder)
                } else {
                    repository?.addFolder(_folderState.value.folder.apply {
                        accountId = currentAccount!!.id
                    })
                }
            } catch (e: Exception) {
                _folderState.update { it.copy(exception = e) }
                return@launch
            }

            closeDialog(DialogState.AddFolder)
        }
    }

    fun resetException() = _feedState.update { it.copy(exception = null) }

    // add/update folder
}