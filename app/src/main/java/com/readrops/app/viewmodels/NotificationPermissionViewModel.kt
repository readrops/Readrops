package com.readrops.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.readrops.readropsdb.Database
import com.readrops.readropsdb.entities.Feed
import com.readrops.readropsdb.entities.account.Account

class NotificationPermissionViewModel(application: Application) : AndroidViewModel(application) {

    val database = Database.getInstance(application)
    var account: Account? = null


    fun getFeedsWithNotifPermission(): LiveData<List<Feed>> = database.feedDao()
            .getFeedsForNotifPermission(account?.id!!)
}