package com.readrops.app.compose.timelime

import androidx.lifecycle.viewModelScope
import com.readrops.app.compose.base.TabViewModel
import com.readrops.db.Database
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.queries.ItemsQueryBuilder
import com.readrops.db.queries.QueryFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TimelineViewModel(
        private val database: Database,
) : TabViewModel(database) {

    private val _timelineState = MutableStateFlow<TimelineState>(TimelineState.Loading)
    val timelineState = _timelineState.asStateFlow()

    private var _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch(context = Dispatchers.IO) {
            val query = ItemsQueryBuilder.buildItemsQuery(QueryFilters(accountId = 1))

            database.newItemDao().selectAll(query)
                    .catch { _timelineState.value = TimelineState.Error(Exception(it)) }
                    .collect {
                        _timelineState.value = TimelineState.Loaded(it)
                    }
        }
    }

    fun refreshTimeline() {
        _isRefreshing.value = true
        viewModelScope.launch(context = Dispatchers.IO) {
            repository?.synchronize(null) {

            }

            _isRefreshing.value = false
        }
    }

    override fun invalidate() {

    }
}

sealed class TimelineState {
    object Loading : TimelineState()
    data class Error(val exception: Exception) : TimelineState()
    data class Loaded(val items: List<ItemWithFeed>) : TimelineState()
}

