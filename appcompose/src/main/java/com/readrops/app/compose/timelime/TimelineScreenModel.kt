package com.readrops.app.compose.timelime

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.compose.base.TabScreenModel
import com.readrops.app.compose.repositories.ErrorResult
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
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
                val query = ItemsQueryBuilder.buildItemsQuery(filters, account.config.useSeparateState)

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
                            .cachedIn(screenModelScope),
                        isAccountLocal = account.isLocal
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

        screenModelScope.launch(dispatcher) {
            accountEvent.flatMapConcat { database.newItemDao().selectUnreadNewItemsCount(it.id) }
                .collectLatest { count ->
                    _timelineState.update {
                        it.copy(unreadNewItemsCount = count)
                    }
                }
        }
    }

    fun refreshTimeline() {
        screenModelScope.launch(dispatcher) {
            if (currentAccount!!.isLocal) {
                refreshLocalAccount()
            } else {
                _timelineState.update { it.copy(isRefreshing = true) }

                repository?.synchronize()
                try {
                } catch (e: Exception) {
                    // handle sync exceptions
                    Log.d("TimelineScreenModel", "refreshTimeline: ${e.message}")
                }

                _timelineState.update {
                    it.copy(
                        isRefreshing = false,
                        endSynchronizing = true
                    )
                }
            }
        }
    }

    private suspend fun refreshLocalAccount() {
        val selectedFeeds = when (filters.value.subFilter) {
            SubFilter.FEED -> listOf(
                database.newFeedDao().selectFeed(filters.value.filterFeedId)
            )

            SubFilter.FOLDER -> database.newFeedDao()
                .selectFeedsByFolder(filters.value.filterFolderId)

            else -> listOf()
        }


        _timelineState.update {
            it.copy(
                feedCount = 0,
                feedMax = if (selectedFeeds.isNotEmpty())
                    selectedFeeds.size
                else
                    database.newFeedDao().selectFeedCount(currentAccount!!.id)
            )
        }

        _timelineState.update { it.copy(isRefreshing = true) }

        val results = repository?.synchronize(
            selectedFeeds = selectedFeeds,
            onUpdate = { feed ->
                _timelineState.update {
                    it.copy(
                        currentFeed = feed.name!!,
                        feedCount = it.feedCount + 1
                    )
                }
            }
        )

        _timelineState.update {
            it.copy(
                isRefreshing = false,
                endSynchronizing = true,
                synchronizationErrors = if (results!!.second.isNotEmpty()) results.second else null
            )
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

    fun closeDialog(dialog: DialogState? = null) {
        if (dialog is DialogState.ErrorList) {
            _timelineState.update { it.copy(synchronizationErrors = null) }
        }

        _timelineState.update { it.copy(dialog = null) }
    }

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

@Stable
data class TimelineState(
    val isRefreshing: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val currentFeed: String = "",
    val unreadNewItemsCount: Int = 0,
    val feedCount: Int = 0,
    val feedMax: Int = 0,
    val endSynchronizing: Boolean = false,
    val synchronizationErrors: ErrorResult? = null,
    val filters: QueryFilters = QueryFilters(),
    val filterFeedName: String = "",
    val filterFolderName: String = "",
    val foldersAndFeeds: Map<Folder?, List<Feed>> = emptyMap(),
    val itemState: Flow<PagingData<ItemWithFeed>> = emptyFlow(),
    val dialog: DialogState? = null,
    val isAccountLocal: Boolean = false
) {

    val showSubtitle = filters.subFilter != SubFilter.ALL

    val displayRefreshScreen = isRefreshing && isAccountLocal
}

sealed interface DialogState {
    object ConfirmDialog : DialogState
    object FilterSheet : DialogState
    class ErrorList(val errorResult: ErrorResult) : DialogState
}
