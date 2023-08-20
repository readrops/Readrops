package com.readrops.app.compose.timelime

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.readrops.app.compose.base.TabViewModel
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.app.compose.timelime.drawer.DrawerDefaultItemsSelection
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.queries.ItemsQueryBuilder
import com.readrops.db.queries.QueryFilters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimelineViewModel(
    private val database: Database,
    private val getFoldersWithFeeds: GetFoldersWithFeeds,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TabViewModel(database) {

    private val _timelineState = MutableStateFlow<TimelineState>(TimelineState.Loading)
    val timelineState = _timelineState.asStateFlow()

    private var _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _drawerState = MutableStateFlow(DrawerState())
    val drawerState = _drawerState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            accountEvent.consumeAsFlow().collectLatest { account ->
                val query = ItemsQueryBuilder.buildItemsQuery(QueryFilters(accountId = account.id))

                val items = async {
                    database.newItemDao().selectAll(query)
                        .catch { _timelineState.value = TimelineState.Error(Exception(it)) }
                        .collect {
                            _timelineState.value = TimelineState.Loaded(it)
                        }
                }

                val drawer = async {
                    _drawerState.update {
                        it.copy(
                            foldersAndFeeds = getFoldersWithFeeds.get(account.id)
                        )
                    }
                }

                awaitAll(items, drawer)
            }
        }
    }

    fun refreshTimeline() {
        _isRefreshing.value = true
        viewModelScope.launch(dispatcher) {
            repository?.synchronize(null) {

            }

            _isRefreshing.value = false
        }
    }

    fun openDrawer() {
        _drawerState.update { it.copy(isOpen = true) }
    }

    fun closeDrawer() {
        _drawerState.update { it.copy(isOpen = false) }
    }

    fun updateDrawerDefaultItem(selection: DrawerDefaultItemsSelection) {
        _drawerState.update {
            it.copy(
                isOpen = false,
                selection = selection,
                selectedFolderId = 0,
                selectedFeedId = 0,
            )
        }
    }

    fun updateDrawerFolderSelection(folderId: Int) {
        _drawerState.update {
            it.copy(
                isOpen = false,
                selectedFolderId = folderId,
                selectedFeedId = 0
            )
        }
    }

    fun updateDrawerFeedSelection(feedId: Int) {
        _drawerState.update {
            it.copy(
                isOpen = false,
                selectedFeedId = feedId,
                selectedFolderId = 0
            )
        }
    }
}

sealed class TimelineState {
    object Loading : TimelineState()

    @Immutable
    data class Error(val exception: Exception) : TimelineState()

    @Immutable
    data class Loaded(val items: List<ItemWithFeed>) : TimelineState()
}

@Immutable
data class DrawerState(
    val isOpen: Boolean = false,
    val selection: DrawerDefaultItemsSelection = DrawerDefaultItemsSelection.ARTICLES,
    val selectedFolderId: Int = 0,
    val selectedFeedId: Int = 0,
    val foldersAndFeeds: Map<Folder?, List<Feed>> = emptyMap()
)

