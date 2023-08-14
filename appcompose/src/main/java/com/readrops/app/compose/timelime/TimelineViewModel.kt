package com.readrops.app.compose.timelime

import androidx.lifecycle.viewModelScope
import com.readrops.app.compose.base.TabViewModel
import com.readrops.db.Database
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.queries.ItemsQueryBuilder
import com.readrops.db.queries.QueryFilters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class TimelineViewModel(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TabViewModel(database) {

    private val _timelineState = MutableStateFlow<TimelineState>(TimelineState.Loading)
    val timelineState = _timelineState.asStateFlow()

    private var _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            accountEvent.consumeAsFlow().collectLatest { account ->
                val query = ItemsQueryBuilder.buildItemsQuery(QueryFilters(accountId = account.id))

                database.newItemDao().selectAll(query)
                    .catch { _timelineState.value = TimelineState.Error(Exception(it)) }
                    .collect {
                        _timelineState.value = TimelineState.Loaded(it)
                    }
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
}

sealed class TimelineState {
    object Loading : TimelineState()
    data class Error(val exception: Exception) : TimelineState()
    data class Loaded(val items: List<ItemWithFeed>) : TimelineState()
}

