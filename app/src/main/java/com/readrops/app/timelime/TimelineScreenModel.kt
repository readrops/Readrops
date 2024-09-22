package com.readrops.app.timelime

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Stable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.workDataOf
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.base.TabScreenModel
import com.readrops.app.repositories.ErrorResult
import com.readrops.app.repositories.GetFoldersWithFeeds
import com.readrops.app.sync.SyncWorker
import com.readrops.app.util.Preferences
import com.readrops.app.util.clearSerializables
import com.readrops.app.util.getSerializable
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.filters.MainFilter
import com.readrops.db.filters.OrderField
import com.readrops.db.filters.OrderType
import com.readrops.db.filters.QueryFilters
import com.readrops.db.filters.SubFilter
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.queries.ItemsQueryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineScreenModel(
    private val database: Database,
    private val getFoldersWithFeeds: GetFoldersWithFeeds,
    private val preferences: Preferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TabScreenModel(database) {

    private val _timelineState = MutableStateFlow(TimelineState())
    val timelineState = _timelineState.asStateFlow()

    // separate this from main Timeline state for performances
    // as it will be very often updated
    private val _listIndexState = MutableStateFlow(0)
    val listIndexState = _listIndexState.asStateFlow()

    private val filters = MutableStateFlow(_timelineState.value.filters)

    init {
        screenModelScope.launch(dispatcher) {
            combine(
                accountEvent,
                filters
            ) { account, filters ->
                account to filters.copy(accountId = account.id)
            }.collectLatest { (account, filters) ->
                this@TimelineScreenModel.filters.update { filters }
                buildPager()

                preferences.hideReadFeeds.flow
                    .flatMapLatest { hideReadFeeds ->
                        getFoldersWithFeeds.get(
                            accountId = account.id,
                            mainFilter = filters.mainFilter,
                            useSeparateState = account.config.useSeparateState,
                            hideReadFeeds = hideReadFeeds
                        )
                    }
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
            accountEvent.flatMapLatest {
                getFoldersWithFeeds.getNewItemsUnreadCount(it.id, it.config.useSeparateState)
            }.collectLatest { count ->
                _timelineState.update {
                    it.copy(unreadNewItemsCount = count)
                }
            }
        }

        screenModelScope.launch(dispatcher) {
            getTimelinePreferences()
                .collect { preferences ->
                    _timelineState.update {
                        it.copy(
                            preferences = preferences,
                            filters = updateFilters {
                                it.filters.copy(
                                    showReadItems = preferences.showReadItems,
                                    orderField = preferences.orderField,
                                    orderType = preferences.orderType
                                )
                            }
                        )
                    }
                }
        }
    }

    private fun getTimelinePreferences(): Flow<TimelinePreferences> {
        return combine(
            preferences.timelineItemSize.flow,
            preferences.scrollRead.flow,
            preferences.displayNotificationsPermission.flow,
            preferences.showReadItems.flow,
            preferences.orderField.flow,
            preferences.orderType.flow,
            transform = {
                TimelinePreferences(
                    itemSize = when (it[0]) {
                        "compact" -> TimelineItemSize.COMPACT
                        "regular" -> TimelineItemSize.REGULAR
                        else -> TimelineItemSize.LARGE
                    },
                    markReadOnScroll = it[1] as Boolean,
                    displayNotificationsPermission = it[2] as Boolean,
                    showReadItems = it[3] as Boolean,
                    orderField = OrderField.valueOf(it[4] as String),
                    orderType = OrderType.valueOf(it[5] as String)
                )
            }
        )
    }

    private fun buildPager(empty: Boolean = false) {
        val query = ItemsQueryBuilder.buildItemsQuery(
            filters.value,
            currentAccount!!.config.useSeparateState
        )

        val pager = Pager(
            config = PagingConfig(
                initialLoadSize = 50,
                pageSize = 50,
                prefetchDistance = 15
            ),
            pagingSourceFactory = {
                database.itemDao().selectAll(query)
            },
        ).flow
            .cachedIn(screenModelScope)

        _timelineState.update {
            it.copy(
                itemState = if (!empty) {
                    pager
                } else {
                    emptyFlow()
                },
                isAccountLocal = currentAccount!!.isLocal,
                scrollToTop = true,
                hideReadAllFAB = !currentAccount!!.config.canMarkAllItemsAsRead
            )
        }

        _listIndexState.update { 0 }
    }

    @Suppress("UNCHECKED_CAST")
    fun refreshTimeline(context: Context) {
        buildPager(empty = true)

        screenModelScope.launch(dispatcher) {
            val filterPair = with(filters.value) {
                when (subFilter) {
                    SubFilter.FEED -> SyncWorker.FEED_ID_KEY to feedId
                    SubFilter.FOLDER -> SyncWorker.FOLDER_ID_KEY to folderId
                    else -> null
                }
            }
            val accountPair = SyncWorker.ACCOUNT_ID_KEY to currentAccount!!.id

            val workData = if (filterPair != null) {
                workDataOf(filterPair, accountPair)
            } else {
                workDataOf(accountPair)
            }

            if (!currentAccount!!.isLocal) {
                _timelineState.update {
                    it.copy(
                        isRefreshing = true,
                        hideReadAllFAB = true
                    )
                }
            }

            SyncWorker.startNow(context, workData) { workInfo ->
                when {
                    workInfo.outputData.getBoolean(SyncWorker.END_SYNC_KEY, false) -> {
                        val errors =
                            workInfo.outputData.getSerializable(SyncWorker.LOCAL_SYNC_ERRORS_KEY) as ErrorResult?
                        workInfo.outputData.clearSerializables()

                        _timelineState.update {
                            it.copy(
                                isRefreshing = false,
                                hideReadAllFAB = false,
                                scrollToTop = true,
                                localSyncErrors = errors?.ifEmpty { null }
                            )
                        }

                        buildPager()
                    }
                    workInfo.outputData.getBoolean(SyncWorker.SYNC_FAILURE_KEY, false) -> {
                        val error =
                            workInfo.outputData.getSerializable(SyncWorker.SYNC_FAILURE_EXCEPTION_KEY) as Exception?
                        workInfo.outputData.clearSerializables()

                        _timelineState.update {
                            it.copy(
                                syncError = error,
                                isRefreshing = false,
                                hideReadAllFAB = false
                            )
                        }

                        buildPager()
                    }
                    workInfo.progress.getString(SyncWorker.FEED_NAME_KEY) != null -> {
                        _timelineState.update {
                            it.copy(
                                isRefreshing = true,
                                currentFeed = workInfo.progress.getString(SyncWorker.FEED_NAME_KEY)
                                    ?: "",
                                feedCount = workInfo.progress.getInt(SyncWorker.FEED_COUNT_KEY, 0),
                                feedMax = workInfo.progress.getInt(SyncWorker.FEED_MAX_KEY, 0)
                            )
                        }
                    }
                }
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
                        feedId = 0,
                        folderId = 0
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
                        folderId = folder.id,
                        feedId = 0
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
                        feedId = feed.id,
                        folderId = 0
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

        screenModelScope.launch(dispatcher) {
            repository?.setItemReadState(item)
        }
    }

    fun updateItemReadState(item: Item) {
        screenModelScope.launch(dispatcher) {
            with(item) {
                isRead = !isRead
                repository?.setItemReadState(this)
            }
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
            when (_timelineState.value.filters.subFilter) {
                SubFilter.FEED ->
                    repository?.setAllItemsReadByFeed(
                        feedId = _timelineState.value.filters.feedId
                    )

                SubFilter.FOLDER -> repository?.setAllItemsReadByFolder(
                    folderId = _timelineState.value.filters.folderId
                )

                else -> when (_timelineState.value.filters.mainFilter) {
                    MainFilter.STARS -> repository?.setAllStarredItemsRead()
                    MainFilter.ALL -> repository?.setAllItemsRead()
                    MainFilter.NEW -> repository?.setAllNewItemsRead()
                }
            }
        }
    }

    fun openDialog(dialog: DialogState) = _timelineState.update { it.copy(dialog = dialog) }

    fun closeDialog(dialog: DialogState? = null) {
        if (dialog is DialogState.ErrorList) {
            _timelineState.update { it.copy(localSyncErrors = null) }
        }

        _timelineState.update { it.copy(dialog = null) }
    }

    fun setShowReadItemsState(showReadItems: Boolean) {
        screenModelScope.launch {
            preferences.showReadItems.write(showReadItems)

            _timelineState.update {
                it.copy(
                    filters = it.filters.copy(showReadItems = showReadItems)
                )
            }
        }
    }

    fun setOrderFieldState(orderField: OrderField) {
        screenModelScope.launch {
            preferences.orderField.write(orderField.name)

            _timelineState.update {
                it.copy(
                    filters = it.filters.copy(orderField = orderField)
                )
            }
        }
    }

    fun setOrderTypeState(orderType: OrderType) {
        screenModelScope.launch {
            preferences.orderType.write(orderType.name)

            _timelineState.update {
                it.copy(filters = it.filters.copy(orderType = orderType))
            }
        }
    }

    fun resetScrollToTop() {
        _timelineState.update { it.copy(scrollToTop = false) }
    }

    fun resetSyncError() {
        _timelineState.update { it.copy(syncError = null) }
    }

    fun updateLastFirstVisibleItemIndex(index: Int) {
        _listIndexState.update { index }
    }

    fun disableDisplayNotificationsPermission() {
        screenModelScope.launch {
            preferences.displayNotificationsPermission.write(false)
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
    val scrollToTop: Boolean = false,
    val localSyncErrors: ErrorResult? = null,
    val syncError: Exception? = null,
    val filters: QueryFilters = QueryFilters(),
    val filterFeedName: String = "",
    val filterFolderName: String = "",
    val foldersAndFeeds: Map<Folder?, List<Feed>> = emptyMap(),
    val itemState: Flow<PagingData<ItemWithFeed>> = emptyFlow(),
    val dialog: DialogState? = null,
    val isAccountLocal: Boolean = false,
    val hideReadAllFAB: Boolean = false,
    val preferences: TimelinePreferences = TimelinePreferences()
) {

    val showSubtitle = filters.subFilter != SubFilter.ALL

    val displayRefreshScreen = isRefreshing && isAccountLocal
}

@Stable
data class TimelinePreferences(
    val itemSize: TimelineItemSize = TimelineItemSize.LARGE,
    val markReadOnScroll: Boolean = false,
    val displayNotificationsPermission: Boolean = false,
    val showReadItems: Boolean = true,
    val orderField: OrderField = OrderField.DATE,
    val orderType: OrderType = OrderType.DESC
)

sealed interface DialogState {
    data object ConfirmDialog : DialogState
    data object FilterSheet : DialogState
    class ErrorList(val errorResult: ErrorResult) : DialogState
}
