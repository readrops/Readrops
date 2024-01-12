package com.readrops.app.compose.feeds

import androidx.lifecycle.viewModelScope
import com.readrops.app.compose.base.TabViewModel
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.launch

class FeedViewModel(
    database: Database,
    private val getFoldersWithFeeds: GetFoldersWithFeeds
) : TabViewModel(database) {

    private val _feedsState = MutableStateFlow<FeedsState>(FeedsState.InitialState)
    val feedsState = _feedsState.asStateFlow()

    init {
        viewModelScope.launch(context = Dispatchers.IO) {
            accountEvent.consumeAsFlow()
                .flatMapConcat { account ->
                    getFoldersWithFeeds.get(account.id)
                }
                .catch { _feedsState.value = FeedsState.ErrorState(Exception(it)) }
                .collect { _feedsState.value = FeedsState.LoadedState(it) }
        }
    }

    fun insertFeed(url: String) {
        viewModelScope.launch(context = Dispatchers.IO) {
            repository?.insertNewFeeds(listOf(url))
        }
    }
}

sealed class FeedsState {
    object InitialState : FeedsState()
    data class ErrorState(val exception: Exception) : FeedsState()
    data class LoadedState(val foldersAndFeeds: Map<Folder?, List<Feed>>) : FeedsState()
}