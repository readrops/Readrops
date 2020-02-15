package com.readrops.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.readrops.app.R
import com.readrops.readropsdb.Database
import com.readrops.readropsdb.entities.account.Account
import com.readrops.readropslibrary.services.SyncResult

/**
 * Simple class to get synchro notification content (title, content and largeIcon) according to some rules
 */
class SyncResultAnalyser(val context: Context, private val syncResults: Map<Account, SyncResult>, val database: Database) {

    fun getSyncNotifContent(): SyncResultNotifContent {
        var title: String? = null
        var contentText: String? = null
        var largeIcon: Bitmap? = null

        if (newItemsInMultipleAccounts()) {
            var itemsNb = 0
            syncResults.values.forEach { itemsNb += it.items.size }

            title = "Notifications"
            contentText = context.getString(R.string.new_items, itemsNb.toString())
        } else { // new items from only one account
            val syncResultMap = syncResults.filterValues { it.items.isNotEmpty() }

            if (syncResultMap.values.isNotEmpty()) {
                val syncResult = syncResultMap.values.first()
                val feedsIdsForNewItems = getFeedsIdsForNewItems(syncResult)

                // new items from several feeds from one account
                if (feedsIdsForNewItems.size > 1) {
                    val account = syncResultMap.keys.first()

                    title = account.accountName
                    contentText = context.getString(R.string.new_items, syncResult.items.size.toString())
                    largeIcon = BitmapFactory.decodeResource(context.resources, account.accountType.iconRes)
                } else if (feedsIdsForNewItems.size == 1) { // new items from only one feed from one account
                    val feed = database.feedDao().getFeedById(feedsIdsForNewItems.first())
                    title = feed?.name

                    feed?.iconUrl?.let {
                        val target = GlideApp.with(context)
                                .asBitmap()
                                .load(it)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .submit()
                        
                        largeIcon = target.get()
                    }

                    contentText = if (syncResult.items.size == 1)
                        syncResult.items.first().title
                    else context.getString(R.string.new_items, syncResult.items.size.toString())
                }
            }

        }

        return SyncResultNotifContent(title,
                contentText,
                largeIcon)
    }

    private fun newItemsInMultipleAccounts(): Boolean {
        val itemsNotEmptyByAccount = mutableListOf<Boolean>()

        for ((_, syncResult) in syncResults) {
            itemsNotEmptyByAccount += syncResult.items.isNotEmpty()
        }

        // return true it there is at least two true booleans in the list
        return itemsNotEmptyByAccount.groupingBy { it }.eachCount()[true] ?: 0 > 1
    }

    private fun getFeedsIdsForNewItems(syncResult: SyncResult): List<Int> {
        val feedsIds = mutableListOf<Int>()

        syncResult.items.forEach {
            if (it.feedId !in feedsIds)
                feedsIds += it.feedId
        }

        return feedsIds
    }
}