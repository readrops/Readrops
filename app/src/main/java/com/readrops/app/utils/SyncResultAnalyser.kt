package com.readrops.app.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.readrops.app.R
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.api.services.SyncResult
import org.koin.core.KoinComponent
import org.koin.core.get

/**
 * Simple class to get synchro notification content (title, content and largeIcon) according to some rules
 */
class SyncResultAnalyser(val context: Context, private val syncResults: Map<Account, SyncResult>, val database: Database) : KoinComponent {

    private val notifContent = SyncResultNotifContent()

    fun getSyncNotifContent(): SyncResultNotifContent {
        if (newItemsInMultipleAccounts()) { // new items from several accounts
            var itemCount = 0
            val feeds = database.feedDao().selectFromIdList(getFeedsIdsForNewItems(syncResults))

            syncResults.values.forEach { syncResult ->
                itemCount += syncResult.items.filter { isFeedNotificationEnabledForItem(feeds, it) }.size
            }

            notifContent.title = context.getString(R.string.new_items, itemCount.toString())
        } else { // new items from only one account
            val syncResultMap = syncResults.filterValues { it.items.isNotEmpty() }

            if (syncResultMap.values.isNotEmpty()) {
                val syncResult = syncResultMap.values.first()
                val account = syncResultMap.keys.first()
                val feedsIdsForNewItems = getFeedsIdsForNewItems(syncResult)

                notifContent.accountId = account.id

                if (account.isNotificationsEnabled) {
                    val feeds = database.feedDao().selectFromIdList(feedsIdsForNewItems)

                    val items = syncResult.items.filter { isFeedNotificationEnabledForItem(feeds, it) }
                    val itemCount = items.size

                    // new items from several feeds from one account
                    if (feedsIdsForNewItems.size > 1 && itemCount > 1) {
                        notifContent.title = account.accountName
                        notifContent.content = context.getString(R.string.new_items, itemCount.toString())
                        notifContent.largeIcon = Utils.getBitmapFromDrawable(ContextCompat.getDrawable(context, account.accountType.iconRes))
                    } else if (feedsIdsForNewItems.size == 1) // new items from only one feed from one account
                        oneFeedCase(feedsIdsForNewItems.first(), syncResult.items)
                    else if (itemCount == 1)
                        oneFeedCase(items.first().feedId.toLong(), items)
                }
            }
        }

        return notifContent
    }

    private fun oneFeedCase(feedId: Long, items: List<Item>) {
        val feed = database.feedDao().getFeedById(feedId.toInt())

        if (feed.isNotificationEnabled) {
            notifContent.title = feed?.name

            feed?.iconUrl?.let {
                val target = get<GlideRequests>()
                        .asBitmap()
                        .load(it)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .submit()

                notifContent.largeIcon = target.get()
            }

            if (items.size == 1) {
                val item = database.itemDao().selectByRemoteId(items.first().remoteId,
                        items.first().feedId)
                notifContent.content = item.title
                notifContent.item = item
            } else notifContent.content = context.getString(R.string.new_items, items.size.toString())
        }
    }

    private fun newItemsInMultipleAccounts(): Boolean {
        val itemsNotEmptyByAccount = mutableListOf<Boolean>()

        for ((account, syncResult) in syncResults) {
            if (account.isNotificationsEnabled) itemsNotEmptyByAccount += syncResult.items.isNotEmpty()
        }

        // return true it there is at least two true booleans in the list
        return itemsNotEmptyByAccount.groupingBy { it }.eachCount()[true] ?: 0 > 1
    }

    private fun getFeedsIdsForNewItems(syncResult: SyncResult): List<Long> {
        val feedsIds = mutableListOf<Long>()

        syncResult.items.forEach {
            if (it.feedId.toLong() !in feedsIds)
                feedsIds += it.feedId.toLong()
        }

        return feedsIds
    }

    private fun getFeedsIdsForNewItems(syncResults: Map<Account, SyncResult>): List<Long> {
        val feedsIds = mutableListOf<Long>()

        syncResults.values.forEach { feedsIds += getFeedsIdsForNewItems(it) }
        return feedsIds
    }

    private fun isFeedNotificationEnabledForItem(feeds: List<Feed>, item: Item): Boolean =
            feeds.find { it.id == item.feedId }?.isNotificationEnabled!!
}