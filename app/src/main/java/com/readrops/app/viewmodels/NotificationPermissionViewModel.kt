package com.readrops.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import io.reactivex.Completable

class NotificationPermissionViewModel(application: Application) : AndroidViewModel(application) {

    val database: Database = Database.getInstance(application)
    var account: Account? = null

    fun getAccount(accountId: Int): LiveData<Account> = database.accountDao().selectAsync(accountId)

    fun getFeedsWithNotifPermission(): LiveData<List<Feed>> = database.feedDao()
            .getFeedsForNotifPermission(account?.id!!)

    fun setAccountNotificationsState(enabled: Boolean): Completable = database.accountDao()
            .updateNotificationState(account?.id!!, enabled)

    fun setFeedNotificationState(feed: Feed): Completable = database.feedDao()
            .updateFeedNotificationState(feed.id, !feed.isNotificationEnabled)

    fun setAllFeedsNotificationState(enabled: Boolean): Completable = database.feedDao()
            .updateAllFeedsNotificationState(account?.id!!, enabled)
}