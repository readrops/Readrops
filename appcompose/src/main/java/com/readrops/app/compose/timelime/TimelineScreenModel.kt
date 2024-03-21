package com.readrops.app.compose.timelime

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.compose.base.TabScreenModel
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.filters.ListSortType
import com.readrops.db.filters.MainFilter
import com.readrops.db.filters.SubFilter
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

class TimelineScreenModel(
    private val database: Database,
    private val getFoldersWithFeeds: GetFoldersWithFeeds,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TabScreenModel(database) {

    private val _timelineState = MutableStateFlow(TimelineState())
    val timelineState = _timelineState.asStateFlow()

    private val filters = MutableStateFlow(_timelineState.value.filters)

    init {
        screenModelScope.launch(dispatcher) {
            combine(
                accountEvent,
                filters
            ) { account, filters ->
                Pair(account, filters.copy(accountId = account.id))
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
                            .cachedIn(screenModelScope)
                    )
                }

                getFoldersWithFeeds.get(account.id, filters.mainFilter)
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

        screenModelScope.launch(dispatcher) {
            val selectedFeeds = if (currentAccount!!.isLocal) {
                when (filters.value.subFilter) {
                    SubFilter.FEED -> listOf(database.newFeedDao().selectFeed(filters.value.filterFeedId))
                    SubFilter.FOLDER -> database.newFeedDao().selectFeedsByFolder(filters.value.filterFolderId)
                    else -> listOf()
                }
            } else listOf()

            repository?.synchronize(
                selectedFeeds = selectedFeeds,
                onUpdate = { }
            )

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

    fun updateDrawerDefaultItem(selection: MainFilter) {
        _timelineState.update {
            it.copy(
                filters = updateFilters {
                    it.filters.copy(
                        mainFilter = selection,
                        subFilter = SubFilter.ALL,
                        filterFeedId = 0,
                        filterFolderId = 0
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
                        subFilter = SubFilter.FOLDER,
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
                        subFilter = SubFilter.FEED,
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
        screenModelScope.launch(dispatcher) {
            repository?.setItemReadState(item)
        }
    }

    fun updateStarState(item: Item) {
        screenModelScope.launch(dispatcher) {
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
        screenModelScope.launch(dispatcher) {
            val accountId = currentAccount!!.id

            when (_timelineState.value.filters.subFilter) {
                SubFilter.FEED ->
                    repository?.setAllItemsReadByFeed(
                        feedId = _timelineState.value.filters.filterFeedId,
                        accountId = accountId
                    )

                SubFilter.FOLDER -> repository?.setAllItemsReadByFolder(
                    folderId = _timelineState.value.filters.filterFolderId,
                    accountId = accountId
                )

                else -> when (_timelineState.value.filters.mainFilter) {
                    MainFilter.STARS -> repository?.setAllStarredItemsRead(accountId)
                    MainFilter.ALL -> repository?.setAllItemsRead(accountId)
                    MainFilter.NEW -> repository?.setAllNewItemsRead(accountId)
                }
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
) {

    val showSubtitle = filters.subFilter != SubFilter.ALL
}

sealed interface DialogState {
    object ConfirmDialog : DialogState
    object FilterSheet : DialogState
}
