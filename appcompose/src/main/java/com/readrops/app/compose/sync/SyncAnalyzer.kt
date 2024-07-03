package com.readrops.app.compose.sync

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.readrops.api.services.SyncResult
import com.readrops.app.compose.R
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import org.koin.core.component.KoinComponent

data class NotificationContent(
    val title: String? = null,
    val content: String? = null,
    val largeIcon: Bitmap? = null,
    val item: Item? = null,
    val accountId: Int = 0
)

class SyncAnalyzer(
    val context: Context,
    val database: Database
) : KoinComponent {

    suspend fun getNotificationContent(syncResults: Map<Account, SyncResult>): NotificationContent {
        return if (newItemsInMultipleAccounts(syncResults)) { // new items from several accounts
            val feeds = database.feedDao().selectFromIds(getFeedsIdsForNewItems(syncResults))

            var itemCount = 0
            syncResults.values.forEach { syncResult ->
                itemCount += syncResult.items.filter {
                    isFeedNotificationEnabledForItem(feeds, it)
                }.size
            }

            NotificationContent(title = context.getString(R.string.new_items, itemCount.toString()))
        } else { // new items from only one account
            getContentFromOneAccount(syncResults)
        }
    }

    private suspend fun getContentFromOneAccount(syncResults: Map<Account, SyncResult>): NotificationContent {
        val syncResultMap = syncResults.filterValues { it.items.isNotEmpty() }

        if (syncResultMap.values.isNotEmpty()) {
            val account = syncResultMap.keys.first()
            val syncResult = syncResultMap.values.first()
            val feedsIdsForNewItems = getFeedsIdsForNewItems(syncResult)

            if (account.isNotificationsEnabled) {
                val feeds = database.feedDao().selectFromIds(feedsIdsForNewItems)

                val items =
                    syncResult.items.filter { isFeedNotificationEnabledForItem(feeds, it) }
                val itemCount = items.size

                // new items from several feeds from one account
                return when {
                    feedsIdsForNewItems.size > 1 && itemCount > 1 -> {
                        NotificationContent(
                            title = account.accountName!!,
                            content = context.getString(R.string.new_items, itemCount.toString()),
                            largeIcon = ContextCompat.getDrawable(
                                context,
                                account.accountType!!.iconRes
                            )!!.toBitmap(),
                            accountId = account.id
                        )
                    }
                    // new items from only one feed from one account
                    feedsIdsForNewItems.size == 1 ->
                        oneFeedCase(feedsIdsForNewItems.first(), syncResult.items, account)

                    itemCount == 1 -> oneFeedCase(items.first().feedId, items, account)
                    else -> NotificationContent()
                }
            }
        }

        return NotificationContent()
    }

    private suspend fun oneFeedCase(
        feedId: Int,
        items: List<Item>,
        account: Account
    ): NotificationContent {
        val feed = database.feedDao().selectFeed(feedId)

        if (feed.isNotificationEnabled) {
            val icon = feed.iconUrl?.let {
                val target = context.imageLoader
                    .execute(
                        ImageRequest.Builder(context)
                            .data(it)
                            .build()
                    )

                target.drawable!!.toBitmap()
            }

            val (item, content) = if (items.size == 1) {
                val item = database.itemDao().selectByRemoteId(
                    items.first().remoteId!!,
                    items.first().feedId
                )

                item to item.title
            } else {
                null to context.getString(R.string.new_items, items.size.toString())
            }

            return NotificationContent(
                title = feed.name,
                largeIcon = icon,
                content = content,
                item = item,
                accountId = account.id
            )
        }

        return NotificationContent()
    }

    private fun newItemsInMultipleAccounts(syncResults: Map<Account, SyncResult>): Boolean {
        val itemsNotEmptyByAccount = mutableListOf<Boolean>()

        for ((account, syncResult) in syncResults) {
            if (account.isNotificationsEnabled) {
                itemsNotEmptyByAccount += syncResult.items.isNotEmpty()
            }
        }

        // return true it there is at least two true in the list
        return (itemsNotEmptyByAccount.groupingBy { it }.eachCount()[true] ?: 0) > 1
    }

    private fun getFeedsIdsForNewItems(syncResult: SyncResult): List<Int> {
        val feedsIds = mutableListOf<Int>()

        syncResult.items.forEach {
            if (it.feedId !in feedsIds)
                feedsIds += it.feedId
        }

        return feedsIds
    }

    private fun getFeedsIdsForNewItems(syncResults: Map<Account, SyncResult>): List<Int> {
        val feedsIds = mutableListOf<Int>()

        syncResults.values.forEach { feedsIds += getFeedsIdsForNewItems(it) }
        return feedsIds
    }

    private fun isFeedNotificationEnabledForItem(feeds: List<Feed>, item: Item): Boolean =
        feeds.find { it.id == item.feedId }?.isNotificationEnabled!!
}