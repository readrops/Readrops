package com.readrops.app.sync

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.readrops.app.R
import com.readrops.app.repositories.SyncResult
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account

data class NotificationContent(
    val title: String? = null,
    val text: String? = null,
    val largeIcon: Bitmap? = null,
    val item: Item? = null,
    val color: Int = 0,
    val accountId: Int = 0
)

class SyncAnalyzer(
    val context: Context,
    val database: Database
) {

    suspend fun getNotificationContent(syncResults: Map<Account, SyncResult>): NotificationContent? {
        return if (newItemsInMultipleAccounts(syncResults)) { // new items from several accounts
            val feeds = database.feedDao().selectFromIds(getNewItemsFeedIds(syncResults))

            val itemCount = syncResults.values.sumOf {
                it.items.count { isFeedNotificationEnabledForItem(feeds, it) }
            }

            NotificationContent(title = context.getString(R.string.new_items, "$itemCount"))
        } else {
            // new items from a single account
            return if (syncResults.isNotEmpty()) {
                getSingleAccountContent(syncResults.keys.first(), syncResults.values.first())
            } else {
                null
            }
        }
    }

    private suspend fun getSingleAccountContent(
        account: Account,
        syncResult: SyncResult
    ): NotificationContent? {
        if (account.isNotificationsEnabled) {
            val feedIds = getNewItemsFeedIds(syncResult)
            val feeds = database.feedDao().selectFromIds(feedIds)

            val items = syncResult.items.filter { isFeedNotificationEnabledForItem(feeds, it) }
            val itemCount = items.size

            return when {
                // multiple new items from several feeds
                feedIds.size > 1 && itemCount > 1 -> {
                    NotificationContent(
                        title = account.name!!,
                        text = context.getString(R.string.new_items, itemCount.toString()),
                        largeIcon = ContextCompat.getDrawable(
                            context,
                            account.type!!.iconRes
                        )!!.toBitmap(),
                        accountId = account.id
                    )
                }
                // multiple new items from a single feed
                feedIds.size == 1 -> singleFeedCase(feedIds.first(), syncResult.items, account)
                // only one new item from a single feed
                itemCount == 1 -> singleFeedCase(items.first().feedId, items, account)
                else -> null
            }
        }

        return null
    }

    private suspend fun singleFeedCase(
        feedId: Int,
        items: List<Item>,
        account: Account
    ): NotificationContent? {
        val feed = database.feedDao().selectFeed(feedId)

        return if (feed.isNotificationEnabled) {
            val icon = feed.iconUrl?.let {
                val target = context.imageLoader
                    .execute(
                        ImageRequest.Builder(context)
                            .data(it)
                            .build()
                    )

                target.image?.toBitmap()
            }

            val (item, text) = if (items.size == 1) {
                val item = items.first()
                item to item.title
            } else {
                null to context.getString(R.string.new_items, items.size.toString())
            }

            NotificationContent(
                title = feed.name,
                text = text,
                largeIcon = icon,
                item = item,
                color = feed.color,
                accountId = account.id
            )
        } else {
            null
        }
    }

    /**
     * Return true if at least two accounts have new items and notifications enabled
     */
    private fun newItemsInMultipleAccounts(syncResults: Map<Account, SyncResult>): Boolean {
        return (syncResults.filter { it.key.isNotificationsEnabled }
            .map { it.value.items.isNotEmpty() }
            .groupingBy { it }
            .eachCount()[true] ?: 0) > 1
    }

    private fun getNewItemsFeedIds(syncResult: SyncResult): List<Int> =
        syncResult.items.map { it.feedId }
            .distinct()

    private fun getNewItemsFeedIds(syncResults: Map<Account, SyncResult>): List<Int> =
        syncResults.values.map { getNewItemsFeedIds(it) }
            .flatten()

    private fun isFeedNotificationEnabledForItem(feeds: List<Feed>, item: Item): Boolean =
        feeds.find { it.id == item.feedId }?.isNotificationEnabled!!
}