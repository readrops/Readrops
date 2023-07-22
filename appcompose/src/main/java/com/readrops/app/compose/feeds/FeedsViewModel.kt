package com.readrops.app.compose.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readrops.app.compose.repositories.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeedsViewModel(
    private val repository: BaseRepository,
) : ViewModel() {

    fun insertFeed(url: String) {
        viewModelScope.launch(context = Dispatchers.IO) {
            repository.insertNewFeeds(listOf(url))
        }
    }
}