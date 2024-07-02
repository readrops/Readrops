package com.readrops.app.compose.notifications

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.FeedWithFolder2
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsScreenModel(
    private val account: Account,
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<NotificationsState>(NotificationsState(isNotificationsEnabled = account.isNotificationsEnabled)) {

    init {
        screenModelScope.launch(dispatcher) {
            database.newAccountDao().selectAccountNotificationsState(account.id)
                .collect { isNotificationsEnabled ->
                    mutableState.update { it.copy(isNotificationsEnabled = isNotificationsEnabled) }
                }
        }

        screenModelScope.launch(dispatcher) {
            database.newFeedDao().selectFeedsWithFolderName(account.id)
                .collect { feedsWithFolder ->
                    mutableState.update { it.copy(feedsWithFolder = feedsWithFolder) }
                }
        }
    }

    fun setAccountNotificationsState(enabled: Boolean) {
        screenModelScope.launch(dispatcher) {
            database.newAccountDao().updateNotificationState(account.id, enabled)
        }
    }

    fun setFeedNotificationsState(feedId: Int, enabled: Boolean) {
        screenModelScope.launch(dispatcher) {
            database.newFeedDao().updateFeedNotificationState(feedId, enabled)
        }
    }

    fun setAllFeedsNotificationsState(enabled: Boolean) {
        screenModelScope.launch(dispatcher) {
            database.newFeedDao().updateAllFeedsNotificationState(account.id, enabled)
        }
    }

}

data class NotificationsState(
    val isNotificationsEnabled: Boolean = false,
    val feedsWithFolder: List<FeedWithFolder2> = emptyList()
) {

    val allFeedNotificationsEnabled
        get() = feedsWithFolder.none { !it.feed.isNotificationEnabled }
}