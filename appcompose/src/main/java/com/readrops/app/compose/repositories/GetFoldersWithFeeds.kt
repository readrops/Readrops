package com.readrops.app.compose.repositories

import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetFoldersWithFeeds(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun get(accountId: Int): Map<Folder?, List<Feed>> = withContext(dispatcher) {
        val foldersWithFeeds = mutableMapOf<Folder?, List<Feed>>()
        val folders = database.newFolderDao().selectFoldersByAccount(accountId)

        for (folder in folders) {
            val feeds = database.newFeedDao().selectFeedsByFolder(folder.id)

            for (feed in feeds) {
                feed.unreadCount = database.newItemDao().selectUnreadCount(feed.id)
            }

            foldersWithFeeds[folder] = feeds
        }

        val feedsAlone = database.newFeedDao().selectFeedsAlone(accountId)
        for (feed in feedsAlone) {
            feed.unreadCount = database.newItemDao().selectUnreadCount(feed.id)
        }

        foldersWithFeeds[null] = feedsAlone
        foldersWithFeeds.toSortedMap(nullsLast())
    }
}