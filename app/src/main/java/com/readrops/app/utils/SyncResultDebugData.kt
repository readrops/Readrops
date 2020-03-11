package com.readrops.app.utils

import android.content.Context
import com.readrops.readropsdb.Database
import com.readrops.readropsdb.entities.Item
import com.readrops.readropsdb.entities.account.Account
import com.readrops.readropsdb.entities.account.AccountType
import com.readrops.readropslibrary.services.SyncResult

class SyncResultDebugData {

    companion object {

        fun oneAccountOneFeedOneItem(context: Context): Map<Account, SyncResult> {
            val account1 = Account().apply {
                id = 1
                accountType = AccountType.FRESHRSS
            }

            val item = Database.getInstance(context).itemDao().select(5362)

            return mutableMapOf<Account, SyncResult>().apply {
                put(account1, SyncResult().apply { items = mutableListOf(item) })
            }
        }

        fun oneAccountMultipleFeeds(): Map<Account, SyncResult> {
            val account1 = Account().apply {
                accountName = "Test account"
                id = 1
                accountType = AccountType.FRESHRSS
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