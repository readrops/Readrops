package com.readrops.app.compose.repositories

import com.readrops.api.services.SyncResult
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.account.Account

typealias ErrorResult = Map<Feed, Exception>

interface Repository {

    /**
     * This method is intended for remote accounts.
     */
    suspend fun login(account: Account)

    /**
     * Global synchronization for the local account.
     * @param selectedFeeds feeds to be updated, will fetch all account feeds if list is empty
     * @param onUpdate notify each feed update
     * @return the result of the synchronization used by notifications
     * and errors per feed if occurred to be transmitted to the user
     */
    suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult>

    /**
     * Global synchronization for remote accounts. Unlike the local account, remote accounts
     * won't benefit from synchronization status and granular synchronization
     * @return the result of the synchronization
     */
    suspend fun synchronize(): SyncResult

    /**
     * Insert new feeds by notifying each of them
     * @param newFeeds feeds to insert
     * @param onUpdate notify each feed insertion
     * @return errors by feed
     */
    suspend fun insertNewFeeds(newFeeds: List<Feed>, onUpdate: (Feed) -> Unit): ErrorResult
}

abstract class BaseRepository(
    val database: Database,
    val account: Account,
) : Repository {

    open suspend fun updateFeed(feed: Feed) =
        database.newFeedDao().updateFeedFields(feed.id, feed.name!!, feed.url!!, feed.folderId)

    open suspend fun deleteFeed(feed: Feed) = database.newFeedDao().delete(feed)

    open suspend fun addFolder(folder: Folder) {
        database.newFolderDao().insert(folder)
    }

    open suspend fun updateFolder(folder: Folder) = database.newFolderDao().update(folder)

    open suspend fun deleteFolder(folder: Folder) = database.newFolderDao().delete(folder)

    open suspend fun setItemReadState(item: Item) {
        when {
            account.config.useSeparateState -> {
                database.newItemStateChangeDao().upsertItemReadStateChange(item, account.id, true)
                database.newItemStateDao().upsertItemReadState(
                    ItemState(
                        id = 0,
                        read = item.isRead,
                        starred = item.isStarred,
                        remoteId = item.remoteId!!,
                        accountId = account.id
                    )
                )
            }
            account.isLocal -> {
                database.newItemDao().updateReadState(item.id, item.isRead)
            }
            else -> {
                database.newItemStateChangeDao().upsertItemReadStateChange(item, account.id, false)
                database.newItemDao().updateReadState(item.id, item.isRead)
            }
        }
    }

    open suspend fun setItemStarState(item: Item) {
        when {
            account.config.useSeparateState -> {
                database.newItemStateChangeDao().upsertItemStarStateChange(item, account.id, true)
                database.newItemStateDao().upsertItemStarState(
                    ItemState(
                        id = 0,
                        read = item.isRead,
                        starred = item.isStarred,
                        remoteId = item.remoteId!!,
                        accountId = account.id
                    )
                )
            }
            account.isLocal -> {
                database.newItemDao().updateStarState(item.id, item.isStarred)
            }
            else -> {
                database.newItemStateChangeDao().upsertItemStarStateChange(item, account.id, false)
                database.newItemDao().updateStarState(item.id, item.isStarred)
            }
        }
    }

    open suspend fun setAllItemsRead() {
        val accountId = account.id

        when {
            account.config.useSeparateState -> {
                database.newItemStateChangeDao().upsertAllItemsReadStateChanges(accountId)
                database.newItemStateDao().setAllItemsRead(accountId)
            }
            account.isLocal -> {
                database.newItemDao().setAllItemsRead(account.id)
            }
            else -> {
                database.newItemStateChangeDao().upsertAllItemsReadStateChanges(accountId)
                database.newItemDao().setAllItemsRead(accountId)
            }
        }
    }

    open suspend fun setAllStarredItemsRead() {
        val accountId = account.id

        when {
            account.config.useSeparateState -> {
                database.newItemStateChangeDao().upsertStarredItemReadStateChanges(accountId)
                database.newItemStateDao().setAllStarredItemsRead(accountId)
            }
            account.isLocal -> {
                database.newItemDao().setAllStarredItemsRead(accountId)
            }
            else -> {
                database.newItemStateChangeDao().upsertStarredItemReadStateChanges(accountId)
                database.newItemDao().setAllStarredItemsRead(accountId)
            }
        }
    }

    open suspend fun setAllNewItemsRead() {
        val accountId = account.id

        when {
            account.config.useSeparateState -> {
                database.newItemStateChangeDao().upsertNewItemReadStateChanges(accountId)
                database.newItemStateDao().setAllNewItemsRead(accountId)
            }
            account.isLocal -> {
                database.newItemDao().setAllNewItemsRead(accountId)
            }
            else -> {
                database.newItemStateChangeDao().upsertNewItemReadStateChanges(accountId)
                database.newItemDao().setAllNewItemsRead(accountId)
            }
        }
    }

    open suspend fun setAllItemsReadByFeed(feedId: Int) {
        val accountId = account.id

        when {
            account.config.useSeparateState -> {
                database.newItemStateChangeDao()
                    .upsertItemReadStateChangesByFeed(feedId, accountId)
                database.newItemStateDao().setAllItemsReadByFeed(feedId, accountId)
            }
            account.isLocal -> {
                database.newItemDao().setAllItemsReadByFeed(feedId, accountId)
            }
            else -> {
                database.newItemStateChangeDao().upsertItemReadStateChangesByFeed(feedId, accountId)
                database.newItemDao().setAllItemsReadByFeed(feedId, accountId)
            }
        }
    }

    open suspend fun setAllItemsReadByFolder(folderId: Int) {
        val accountId = account.id

        when {
            account.config.useSeparateState -> {
                database.newItemStateChangeDao().upsertItemReadStateChangesByFolder(folderId, accountId)
                database.newItemStateDao().setAllItemsReadByFolder(folderId, accountId)
            }
            account.isLocal -> {
                database.newItemDao().setAllItemsReadByFolder(folderId, accountId)
            }
            else -> {
                database.newItemStateChangeDao().upsertItemReadStateChangesByFolder(folderId, accountId)
                database.newItemDao().setAllItemsReadByFolder(folderId, accountId)
            }
        }
    }

    suspend fun insertOPMLFoldersAndFeeds(
        foldersAndFeeds: Map<Folder?, List<Feed>>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult {
        val errors = mutableMapOf<Feed, Exception>()

        for ((folder, feeds) in foldersAndFeeds) {
            if (folder != null) {
                folder.accountId = account.id

                val dbFolder = database.newFolderDao().selectFolderByName(folder.name!!, account.id)

                if (dbFolder != null) {
                    folder.id = dbFolder.id
                } else {
                    folder.id = database.newFolderDao().insert(folder).toInt()
                }
            }

            feeds.forEach { it.folderId = folder?.id }

            errors += insertNewFeeds(
                newFeeds = feeds,
                onUpdate = onUpdate
            )
        }

        return errors
    }
}