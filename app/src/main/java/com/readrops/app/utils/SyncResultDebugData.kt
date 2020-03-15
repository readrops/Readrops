package com.readrops.app.utils

import android.content.Context
import com.readrops.readropsdb.Database
import com.readrops.readropsdb.entities.Item
import com.readrops.readropsdb.entities.account.Account
import com.readrops.readropsdb.entities.account.AccountType
import com.readrops.readropslibrary.services.SyncResult
import org.jetbrains.annotations.TestOnly

class SyncResultDebugData {

    companion object {

        @TestOnly
        fun oneAccountOneFeedOneItem(context: Context): Map<Account, SyncResult> {
            val account1 = Account().apply {
                id = 1
                accountType = AccountType.FRESHRSS
                isNotificationsEnabled = true
            }

            val database = Database.getInstance(context)
            val item = database.itemDao().select(5056)
            // database.feedDao().updateNotificationState(item.feedId, false).subscribe()

            return mutableMapOf<Account, SyncResult>().apply {
                put(account1, SyncResult().apply { items = mutableListOf(item) })
            }
        }

        @TestOnly
        fun oneAccountOneFeedMultipleItems(context: Context): Map<Account, SyncResult> {
            val account1 = Account().apply {
                id = 1
                accountType = AccountType.FRESHRSS
                isNotificationsEnabled = true
            }

            val database = Database.getInstance(context)
            val item = database.itemDao().select(5055)
            database.feedDao().updateNotificationState(item.feedId, false).subscribe()

            val item2 = database.itemDao().select(5056)

            return mutableMapOf<Account, SyncResult>().apply {
                put(account1, SyncResult().apply { items = listOf(item, item2) })
            }
        }

        @TestOnly
        fun oneAccountMultipleFeeds(): Map<Account, SyncResult> {
            val account1 = Account().apply {
                accountName = "Test account"
                id = 1
                accountType = AccountType.FRESHRSS
                isNotificationsEnabled = true
            }

            val item1 = Item().apply {
                id = 1
                title = "oneAccountMultipleFeeds"
                feedId = 1
            }

            val item2 = Item().apply {
                id = 2
                title = "oneAccountMultipleFeeds"
                feedId = 2
            }

            return mutableMapOf<Account, SyncResult>().apply {
                put(account1, SyncResult().apply { items = mutableListOf(item1, item2) })
            }
        }

        fun multipleAccounts(): Map<Account, SyncResult> {
            val account1 = Account().apply {
                id = 1
                accountType = AccountType.FRESHRSS
            }

            val account2 = Account().apply {
                id = 2
                accountType = AccountType.LOCAL
            }

            val item = Item().apply {
                id = 1
                title = "multipleAccountsCase"
                feedId = 90
            }

            return mutableMapOf<Account, SyncResult>().apply {
                put(account1, SyncResult().apply { items = mutableListOf(item) })
                put(account2, SyncResult().apply { items = mutableListOf(item) })
            }
        }
    }
}