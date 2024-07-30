package com.readrops.app.notifications

import androidx.core.app.NotificationManagerCompat
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.util.Preferences
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.FeedWithFolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsScreenModel(
    private val account: Account,
    private val database: Database,
    private val preferences: Preferences,
    private val notificationManager: NotificationManagerCompat,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<NotificationsState>(NotificationsState(areAccountNotificationsEnabled = account.isNotificationsEnabled)) {

    init {
        screenModelScope.launch(dispatcher) {
            database.accountDao().selectAccountNotificationsState(account.id)
                .collect { isNotificationsEnabled ->
                    mutableState.update { it.copy(areAccountNotificationsEnabled = isNotificationsEnabled) }
                }
        }

        screenModelScope.launch(dispatcher) {
            database.feedDao().selectFeedsWithFolderName(account.id)
                .collect { feedsWithFolder ->
                    mutableState.update { it.copy(feedsWithFolder = feedsWithFolder) }
                }
        }

        screenModelScope.launch(dispatcher) {
            preferences.backgroundSynchronization.flow
                .collect { sync ->
                    mutableState.update { it.copy(isBackGroundSyncEnabled = sync != "manual") }
                }
        }
    }

    fun setAccountNotificationsState(enabled: Boolean) {
        screenModelScope.launch(dispatcher) {
            database.accountDao().updateNotificationState(account.id, enabled)
        }
    }

    fun setFeedNotificationsState(feedId: Int, enabled: Boolean) {
        screenModelScope.launch(dispatcher) {
            database.feedDao().updateFeedNotificationState(feedId, enabled)
        }
    }

    fun setAllFeedsNotificationsState(enabled: Boolean) {
        screenModelScope.launch(dispatcher) {
            database.feedDao().updateAllFeedsNotificationState(account.id, enabled)
        }
    }

    fun setBackgroundSyncDialogState(visible: Boolean) {
        when {
            visible && !state.value.isBackGroundSyncEnabled -> {
                mutableState.update { it.copy(showBackgroundSyncDialog = visible) }
            }
            !visible -> {
                mutableState.update { it.copy(showBackgroundSyncDialog = visible) }
            }
        }
    }

    fun refreshNotificationManager() {
        mutableState.update { it.copy(areNotificationsEnabled = notificationManager.areNotificationsEnabled()) }
    }
}

data class NotificationsState(
    val areAccountNotificationsEnabled: Boolean = false,
    val feedsWithFolder: List<FeedWithFolder> = emptyList(),
    val showBackgroundSyncDialog: Boolean = false,
    val isBackGroundSyncEnabled: Boolean = false,
    val areNotificationsEnabled: Boolean = false
) {

    val allFeedNotificationsEnabled
        get() = feedsWithFolder.none { !it.feed.isNotificationEnabled }
}