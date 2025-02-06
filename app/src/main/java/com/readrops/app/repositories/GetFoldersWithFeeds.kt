package com.readrops.app.repositories

import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.FoldersWithFeedAndUnreadCount
import com.readrops.db.entities.unbox
import com.readrops.db.filters.MainFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class GetFoldersWithFeeds(
    private val database: Database,
) {
    @Deprecated("Use GetFoldersWithFeeds#get2")
    fun get(
        accountId: Int,
        mainFilter: MainFilter,
        hideReadFeeds: Boolean = false
    ): Flow<Map<Folder?, List<Feed>>> {
        return database.folderDao().selectFolders2(accountId).map { foldersAndFeeds ->
            foldersAndFeeds.associate { folderAndFeeds ->
                var unreadCount = 0
                val resultFeeds = folderAndFeeds.feeds.map {
                    unreadCount += it.unreadCount
                    it.feed.copy(unreadCount = it.unreadCount)
                }
                folderAndFeeds.folder?.copy(unreadCount = unreadCount) to resultFeeds
            }
        }
    }
    fun get2(
        accountId: Int,
        mainFilter: MainFilter,
        hideReadFeeds: Boolean = false
    ): Flow<FoldersWithFeedAndUnreadCount> = database.folderDao().selectFolders2(accountId).map {
        it.unbox()
    }

    fun getNewItemsUnreadCount(accountId: Int, useSeparateState: Boolean): Flow<Int> =
        if (useSeparateState) {
            database.itemDao().selectUnreadNewItemsCountByItemState(accountId)
        } else {
            database.itemDao().selectUnreadNewItemsCount(accountId)
        }
}