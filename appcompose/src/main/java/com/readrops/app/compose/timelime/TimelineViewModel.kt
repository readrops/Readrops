package com.readrops.app.compose.timelime

import androidx.lifecycle.viewModelScope
import com.readrops.app.compose.base.TabViewModel
import com.readrops.db.Database
import com.readrops.db.entities.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TimelineViewModel(
        private val database: Database,
) : TabViewModel(database) {

    private val _timelineState = MutableStateFlow<TimelineState>(TimelineState.InitialState)
    val timelineState = _timelineState.asStateFlow()

    private var _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch(context = Dispatchers.IO) {
            database.newItemDao().selectAll()
                    .catch { _timelineState.value = TimelineState.ErrorState(Exception(it)) }
                    .collect {
                        _timelineState.value = TimelineState.LoadedState(it)
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
        refreshTimeline()
    }
}

sealed class TimelineState {
    object InitialState : TimelineState()
    data class ErrorState(val exception: Exception) : TimelineState()
    data class LoadedState(val items: List<Item>) : TimelineState()
}

