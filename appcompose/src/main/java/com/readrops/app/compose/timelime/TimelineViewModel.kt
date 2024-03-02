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
import com.readrops.db.filters.ListSortType
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
import kotlinx.coroutines.flow.emptyFlow
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
                accountEvent,
                filters
            ) { account, filters ->
                filters.accountId = account.id
                Pair(account, filters)
            }.collectLatest { (account, filters) ->
                val query = ItemsQueryBuilder.buildItemsQuery(filters)

                _timelineState.update {
                    it.copy(
                        itemState = Pager(
                            config = PagingConfig(
                                pageSize = 10,
                                prefetchDistance = 10
                            ),
                            pagingSourceFactory = {
                                database.newItemDao().selectAll(query)
                            },
                        ).flow
                            .cachedIn(viewModelScope)
                    )
                }

                getFoldersWithFeeds.get(account.id, filters.filterType)
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

            _timelineState.update {
                it.copy(
                    isRefreshing = false,
                    endSynchronizing = true
                )
            }
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

    fun updateDrawerFolderSelection(folder: Folder) {
        _timelineState.update {
            it.copy(
                filters = updateFilters {
                    it.filters.copy(
                        filterType = FilterType.FOLDER_FILER,
                        filterFolderId = folder.id,
                        filterFeedId = 0
                    )
                },
                filterFolderName = folder.name!!,
                isDrawerOpen = false
            )
        }
    }

    fun updateDrawerFeedSelection(feed: Feed) {
        _timelineState.update {
            it.copy(
                filters = updateFilters {
                    it.filters.copy(
                        filterType = FilterType.FEED_FILTER,
                        filterFeedId = feed.id,
                        filterFolderId = 0
                    )
                },
                filterFeedName = feed.name!!,
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

    fun setAllItemsRead() {
        viewModelScope.launch(dispatcher) {
            when (_timelineState.value.filters.filterType) {
                FilterType.FEED_FILTER ->
                    repository?.setAllItemsReadByFeed(
                        _timelineState.value.filters.filterFeedId,
                        currentAccount!!.id
                    )

                FilterType.FOLDER_FILER -> repository?.setAllItemsReadByFolder(
                    _timelineState.value.filters.filterFolderId,
                    currentAccount!!.id
                )

                FilterType.STARS_FILTER -> repository?.setAllStarredItemsRead(currentAccount!!.id)
                FilterType.NO_FILTER -> repository?.setAllItemsRead(currentAccount!!.id)
                FilterType.NEW -> TODO()
            }
        }
    }

    fun openDialog(dialog: DialogState) = _timelineState.update { it.copy(dialog = dialog) }

    fun closeDialog() = _timelineState.update { it.copy(dialog = null) }

    fun setShowReadItemsState(showReadItems: Boolean) {
        _timelineState.update {
            it.copy(
                filters = updateFilters {
                    it.filters.copy(
                        showReadItems = showReadItems
                    )
                }
            )
        }
    }

    fun setSortTypeState(sortType: ListSortType) {
        _timelineState.update {
            it.copy(
                filters = updateFilters {
                    it.filters.copy(
                        sortType = sortType
                    )
                }
            )
        }
    }
}

@Immutable
data class TimelineState(
    val isRefreshing: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val endSynchronizing: Boolean = false,
    val filters: QueryFilters = QueryFilters(),
    val filterFeedName: String = "",
    val filterFolderName: String = "",
    val foldersAndFeeds: Map<Folder?, List<Feed>> = emptyMap(),
    val itemState: Flow<PagingData<ItemWithFeed>> = emptyFlow(),
    val dialog: DialogState? = null
)

sealed interface DialogState {
    object ConfirmDialog : DialogState
    object FilterSheet : DialogState
}
