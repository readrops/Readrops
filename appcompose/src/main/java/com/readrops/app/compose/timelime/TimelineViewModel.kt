package com.readrops.app.compose.timelime

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.readrops.app.compose.base.TabViewModel
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.filters.FilterType
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.queries.ItemsQueryBuilder
import com.readrops.db.queries.QueryFilters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimelineViewModel(
    private val database: Database,
    private val getFoldersWithFeeds: GetFoldersWithFeeds,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TabViewModel(database) {

    private val _timelineState = MutableStateFlow(TimelineState())
    val timelineState = _timelineState.asStateFlow()

    private val filters = MutableStateFlow(_timelineState.value.filters)

    init {
        viewModelScope.launch(dispatcher) {
            combine(
                accountEvent.consumeAsFlow(),
                filters
            ) { account, filters ->
                Pair(account, filters)
            }.collectLatest { (account, filters) ->
                val query = ItemsQueryBuilder.buildItemsQuery(filters.copy(accountId = account.id))

                _timelineState.update {
                    it.copy(
                        items = ItemState.Loaded(
                            items = Pager(
                                config = PagingConfig(
                                    pageSize = 100,
                                    prefetchDistance = 150
                                ),
                                pagingSourceFactory = {
                                    database.newItemDao().selectAll(query)
                                },
                            ).flow
                                .cachedIn(viewModelScope)
                        )
                    )
                }

                getFoldersWithFeeds.get(account.id)
                    .collect { foldersAndFeeds ->
                        _timelineState.update {
                            it.copy(
                                foldersAndFeeds = foldersAndFeeds
                            )
                        }
                    }
            }
        }
    }

    fun refreshTimeline() {
        _timelineState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch(dispatcher) {
            repository?.synchronize(null) {

            }

            _timelineState.update { it.copy(isRefreshing = false) }
        }
    }

    fun openDrawer() {
        _timelineState.update { it.copy(isDrawerOpen = true) }
    }

    fun closeDrawer() {
        _timelineState.update { it.copy(isDrawerOpen = false) }
    }

    fun updateDrawerDefaultItem(selection: FilterType) {
        _timelineState.update {
            it.copy(
                filters = updateFilters {
                    it.filters.copy(
                        filterType = selection
                    )
                },
                isDrawerOpen = false
            )
        }
    }

    fun updateDrawerFolderSelection(folderId: Int) {
        _timelineState.update {
            it.copy(
                filters = updateFilters {
                    it.filters.copy(
                        filterType = FilterType.FOLDER_FILER,
                        filterFolderId = folderId,
                        filterFeedId = 0
                    )
                },
                isDrawerOpen = false
            )
        }
    }

    fun updateDrawerFeedSelection(feedId: Int) {
        _timelineState.update {
            it.copy(
                filters = updateFilters {
                    it.filters.copy(
                        filterType = FilterType.FEED_FILTER,
                        filterFeedId = feedId,
                        filterFolderId = 0
                    )
                },
                isDrawerOpen = false
            )
        }
    }

    private fun updateFilters(block: () -> QueryFilters): QueryFilters {
        val filter = block()
        filters.update { filter }

        return filter
    }

    fun setItemRead(item: Item) {
        item.isRead = true
        updateItemReadState(item)
    }

    private fun updateItemReadState(item: Item) {
        viewModelScope.launch(dispatcher) {
            repository?.setItemReadState(item)
        }
    }

    fun updateStarState(item: Item) {
        viewModelScope.launch(dispatcher) {
            with(item) {
                isStarred = isStarred.not()
                repository?.setItemStarState(this)
            }
        }
    }

    fun shareItem(item: Item, context: Context) {
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, item.link)
        }.also {
            context.startActivity(Intent.createChooser(it, null))
        }
    }
}

@Immutable
data class TimelineState(
    val isRefreshing: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val filters: QueryFilters = QueryFilters(),
    val foldersAndFeeds: Map<Folder?, List<Feed>> = emptyMap(),
    val items: ItemState = ItemState.Loading
)

sealed class ItemState {
    @Immutable
    object Loading : ItemState()

    @Immutable
    data class Error(val exception: Exception) : ItemState()

    @Immutable
    data class Loaded(val items: Flow<PagingData<ItemWithFeed>>) : ItemState()
}
