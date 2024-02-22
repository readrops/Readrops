package com.readrops.app.compose.repositories

import com.readrops.api.services.SyncResult
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account

data class ErrorResult(
    val values: Map<Feed, Exception>
)

abstract class ARepository(
    val database: Database,
    val account: Account
) {

    /**
     * This method is intended for remote accounts.
     */
    abstract suspend fun login()

    /**
     * Global synchronization for the local account.
     * @param selectedFeeds feeds to be updated
     * @param onUpdate get synchronization status
     * @return returns the result of the synchronization used by notifications
     * and errors per feed if occurred to be transmitted to the user
     */
    abstract suspend fun synchronize(
        selectedFeeds: List<Feed>?,
        onUpdate: (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult>

    /**
     * Global synchronization for remote accounts. Unlike the local account, remote accounts
     * won't benefit from synchronization status and granular synchronization
     */
    abstract suspend fun synchronize(): SyncResult

    abstract suspend fun insertNewFeeds(urls: List<String>)
}

abstract class BaseRepository(
    database: Database,
    account: Account,
) : ARepository(database, account) {

    open suspend fun updateFeed(feed: Feed) = database.newFeedDao().updateFeedFields(feed.id, feed.name!!, feed.url!!, feed.folderId)

    open suspend fun deleteFeed(feed: Feed) = database.newFeedDao().delete(feed)

    open suspend fun addFolder(folder: Folder) = database.newFolderDao().insert(folder)

    open suspend fun updateFolder(folder: Folder) = database.newFolderDao().update(folder)

    open suspend fun deleteFolder(folder: Folder) = database.newFolderDao().delete(folder)

    open suspend fun setItemReadState(item: Item) {
        database.newItemDao().updateReadState(item.id, item.isRead)
    }

    open suspend fun setItemStarState(item: Item) {
        database.newItemDao().updateStarState(item.id, item.isStarred)
    }

    open suspend fun setAllItemsRead(accountId: Int) {
        database.newItemDao().setAllItemsRead(accountId)
    }

    open suspend fun setAllStarredItemsRead(accountId: Int) {
        database.newItemDao().setAllStarredItemsRead(accountId)
    }

    open suspend fun setAllItemsReadByFeed(feedId: Int, accountId: Int) {
        database.newItemDao().setAllItemsReadByFeed(feedId, accountId)
    }

    open suspend fun setAllItemsReadByFolder(folderId: Int, accountId: Int) {
        database.newItemDao().setAllItemsReadByFolder(folderId, accountId)
    }
}