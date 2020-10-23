package com.readrops.app.notifications.sync

import com.readrops.api.services.SyncResult
import com.readrops.db.Database
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import org.jetbrains.annotations.TestOnly
import org.koin.core.KoinComponent
import org.koin.core.get

class SyncResultDebugData {

    companion object : KoinComponent {

        @TestOnly
        fun oneAccountOneFeedOneItem(): Map<Account, SyncResult> {
            val database = get<Database>()
            val account1 = database.accountDao().select(2)


            val item = database.itemDao().select(5000)
            // database.feedDao().updateNotificationState(item.feedId, false).subscribe()

            return mutableMapOf<Account, SyncResult>().apply {
                put(account1, SyncResult().apply { items = mutableListOf(item) })
            }
        }

        @TestOnly
        fun oneAccountOneFeedMultipleItems(): Map<Account, SyncResult> {
            val account1 = Account().apply {
                id = 1
                accountType = AccountType.FRESHRSS
                isNotificationsEnabled = true
            }

            val database = get<Database>()
            val item = database.itemDao().select(5055)
            database.feedDao().updateFeedNotificationState(item.feedId, false).subscribe()

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
                isNotificationsEnabled = true
            }

            val account2 = Account().apply {
                id = 2
                accountType = AccountType.LOCAL
                isNotificationsEnabled = true
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