package com.readrops.app.feeds

import android.content.Context
import android.util.Patterns
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.R
import com.readrops.app.home.TabScreenModel
import com.readrops.app.repositories.GetFoldersWithFeeds
import com.readrops.app.util.components.TextFieldError
import com.readrops.app.util.components.dialog.TextFieldDialogState
import com.readrops.app.util.extensions.isConnected
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.OpenIn
import com.readrops.db.filters.MainFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalCoroutinesApi::class)
class FeedScreenModel(
    private val database: Database,
    private val getFoldersWithFeeds: GetFoldersWithFeeds,
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TabScreenModel(database, context), KoinComponent {

    private val _feedState = MutableStateFlow(FeedState())
    val feedsState = _feedState.asStateFlow()

    private val _updateFeedDialogState = MutableStateFlow(UpdateFeedDialogState())
    val updateFeedDialogState = _updateFeedDialogState.asStateFlow()

    private val _folderState = MutableStateFlow(TextFieldDialogState())
    val folderState = _folderState.asStateFlow()

    init {
        screenModelScope.launch(dispatcher) {
            accountEvent.flatMapLatest { account ->
                _feedState.update {
                    it.copy(
                        isAccountNotificationsEnabled = account.isNotificationsEnabled,
                        config = account.config
                    )
                }

                _updateFeedDialogState.update {
                    it.copy(
                        isFeedUrlReadOnly = account.config.isFeedUrlReadOnly,
                    )
                }

                getFoldersWithFeeds.get(
                    account.id,
                    MainFilter.ALL,
                    account.config.useSeparateState
                )
            }
                .catch { throwable ->
                    _feedState.update {
                        it.copy(foldersAndFeeds = FolderAndFeedsState.ErrorState(Exception(throwable)))
                    }
                }
                .collect { foldersAndFeeds ->
                    _feedState.update { state ->
                        val dialog = when (state.dialog) {
                            is DialogState.FeedSheet -> {
                                val feed = foldersAndFeeds.values.flatten()
                                    .first { it.id == state.dialog.feed.id }
                                state.dialog.copy(feed = feed)
                            }

                            is DialogState.UpdateFeedOpenInSetting -> {
                                val feed = foldersAndFeeds.values.flatten()
                                    .first { it.id == state.dialog.feed.id }
                                state.dialog.copy(feed = feed)
                            }

                            else -> {
                                state.dialog
                            }
                        }

                        state.copy(
                            foldersAndFeeds = FolderAndFeedsState.LoadedState(foldersAndFeeds),
                            dialog = dialog
                        )
                    }
                }
        }

        screenModelScope.launch(dispatcher) {
            accountEvent.flatMapLatest { account ->
                _updateFeedDialogState.update {
                    it.copy(
                        isFeedUrlReadOnly = account.config.isFeedUrlReadOnly,
                    )
                }

                database.folderDao().selectFolders(account.id)
            }
                .collect { folders ->
                    _updateFeedDialogState.update {
                        it.copy(
                            folders = if (currentAccount!!.config.addNoFolder) {
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
        when (dialog) {
            is DialogState.AddFolder, is DialogState.UpdateFolder -> {
                _folderState.update {
                    it.copy(
                        value = "",
                        textFieldError = null,
                        error = null,
                        isLoading = false
                    )
                }
            }

            is DialogState.UpdateFeed -> {
                _updateFeedDialogState.update { it.copy(error = null, isLoading = false) }
            }

            else -> {}
        }

        if (dialog is DialogState.UpdateFeedOpenInSetting) {
            _feedState.update {
                it.copy(
                    dialog = DialogState.FeedSheet(
                        feed = dialog.feed,
                        folder = null,
                        config = currentAccount!!.config
                    )
                )
            }
        } else {
            _feedState.update { it.copy(dialog = null) }
        }
    }

    fun openDialog(state: DialogState) {
        when (state) {
            is DialogState.UpdateFeed -> {
                _updateFeedDialogState.update {
                    it.copy(
                        feedId = state.feed.id,
                        feedName = state.feed.name!!,
                        feedUrl = state.feed.url!!,
                        selectedFolder = state.folder
                            ?: it.folders.find { folder -> folder.id == 0 },
                        feedRemoteId = state.feed.remoteId
                    )
                }
            }

            is DialogState.UpdateFolder -> {
                _folderState.update {
                    it.copy(
                        value = state.folder.name.orEmpty()
                    )
                }
            }

            else -> {}
        }

        _feedState.update { it.copy(dialog = state) }
    }

    fun deleteFeed(feed: Feed) {
        if (!checkInternetConnection()) {
            return
        }

        screenModelScope.launch(dispatcher) {
            try {
                repository?.deleteFeed(feed)
            } catch (e: Exception) {
                _feedState.update { it.copy(error = accountError?.deleteFeedMessage(e)) }
            }
        }
    }

    fun deleteFolder(folder: Folder) {
        if (!checkInternetConnection()) {
            return
        }

        screenModelScope.launch(dispatcher) {
            try {
                repository?.deleteFolder(folder)
            } catch (e: Exception) {
                _feedState.update { it.copy(error = accountError?.deleteFolderMessage(e)) }
            }
        }
    }

    //region Update feed

    fun setFolderDropDownState(isExpanded: Boolean) {
        _updateFeedDialogState.update {
            it.copy(isFolderDropDownExpanded = isExpanded)
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
                if (!context.isConnected()) {
                    _updateFeedDialogState.update { it.copy(error = context.getString(R.string.no_network)) }
                    return
                } else {
                    _updateFeedDialogState.update { it.copy(error = null, isLoading = true) }
                }

                screenModelScope.launch(dispatcher) {
                    with(_updateFeedDialogState.value) {
                        try {
                            repository?.updateFeed(
                                Feed(
                                    id = feedId,
                                    name = feedName,
                                    url = feedUrl,
                                    folderId = if (selectedFolder?.id != 0)
                                        selectedFolder?.id
                                    else null,
                                    remoteFolderId = selectedFolder?.remoteId,
                                    remoteId = feedRemoteId
                                )
                            )
                        } catch (e: Exception) {
                            _updateFeedDialogState.update {
                                it.copy(
                                    error = accountError?.updateFeedMessage(e),
                                    isLoading = false
                                )
                            }
                            return@launch
                        }
                    }

                    closeDialog(_feedState.value.dialog)
                }
            }
        }
    }

    //endregion

    //region Add/Update folder

    fun setFolderName(name: String) = _folderState.update {
        it.copy(
            value = name,
            textFieldError = null,
        )
    }

    fun folderValidate(updateFolder: Boolean = false) {
        val name = _folderState.value.value

        if (name.isEmpty()) {
            _folderState.update {
                it.copy(
                    textFieldError = TextFieldError.EmptyField,
                    isLoading = false
                )
            }
            return
        }

        if (!context.isConnected()) {
            _folderState.update { it.copy(error = context.getString(R.string.no_network)) }
            return
        } else {
            _folderState.update { it.copy(isLoading = true) }
        }

        screenModelScope.launch(dispatcher) {
            try {
                if (updateFolder) {
                    val folder = (_feedState.value.dialog as DialogState.UpdateFolder).folder
                    repository?.updateFolder(folder.copy(name = name))
                } else {
                    repository?.addFolder(Folder(name = name, accountId = currentAccount!!.id))
                }
            } catch (e: Exception) {
                _folderState.update {
                    it.copy(
                        error = if (updateFolder) {
                            accountError?.updateFolderMessage(e)
                        } else {
                            accountError?.newFolderMessage(e)
                        },
                        isLoading = false
                    )
                }
                return@launch
            }

            closeDialog(_feedState.value.dialog)
        }
    }

    //endregion

    fun resetException() = _feedState.update { it.copy(error = null) }

    fun updateFeedNotifications(feedId: Int, isEnabled: Boolean) {
        screenModelScope.launch(dispatcher) {
            database.feedDao().updateFeedNotificationState(feedId, isEnabled)
        }
    }

    fun updateFeedOpenInSetting(feedId: Int, openIn: OpenIn) {
        screenModelScope.launch(dispatcher) {
            database.feedDao().updateOpenInSetting(feedId, openIn)
        }
    }

    private fun checkInternetConnection(): Boolean {
        if (!currentAccount!!.isLocal) {
            return true
        }

        val isConnected = context.isConnected()

        if (!isConnected) {
            _feedState.update { it.copy(error = context.getString(R.string.no_network)) }
        }

        return isConnected
    }
}