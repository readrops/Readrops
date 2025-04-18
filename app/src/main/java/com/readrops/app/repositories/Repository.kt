package com.readrops.app.repositories

import androidx.room.withTransaction
import com.readrops.api.services.fever.adapters.Favicon
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.account.Account

typealias ErrorResult = HashMap<Feed, Exception>

data class SyncResult(
    val items: List<Item> = listOf(),
    val feeds: List<Feed> = listOf(),
    val favicons: Map<Feed, Favicon> = emptyMap() // only for Fever
)

interface Repository {

    /**
     * This method is intended for remote accounts.
     */
    suspend fun login(account: Account)

    /**
     * Global synchronization for the local account.
     * @param selectedFeeds feeds to be updated, will fetch all account feeds if list is empty
     * @param onUpdate notify each feed update
     * @return newly inserted items and feeds used by background synchronization and notifications,
     * and errors per feed if occurred to be transmitted to the user
     */
    suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: suspend (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult>

    /**
     * Global synchronization for remote accounts. Unlike the local account, remote accounts
     * won't benefit from synchronization status and granular synchronization
     * @return the result of the synchronization: newly inserted items and feeds
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
        database.feedDao().updateFeedFields(feed.id, feed.name!!, feed.url!!, feed.folderId)

    open suspend fun deleteFeed(feed: Feed) = database.feedDao().delete(feed)

    open suspend fun addFolder(folder: Folder) {
        database.folderDao().insert(folder)
    }

    open suspend fun updateFolder(folder: Folder) = database.folderDao().update(folder)

    open suspend fun deleteFolder(folder: Folder) = database.folderDao().delete(folder)

    open suspend fun setItemReadState(item: Item) {
        database.withTransaction {
            when {
                account.config.useSeparateState -> {
                    database.itemStateChangeDao().upsertItemReadStateChange(item, account.id, true)
                    database.itemStateDao().upsertItemReadState(
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
                    database.itemDao().updateReadState(item.id, item.isRead)
                }

                else -> {
                    database.itemStateChangeDao().upsertItemReadStateChange(item, account.id, false)
                    database.itemDao().updateReadState(item.id, item.isRead)
                }
            }
        }
    }

    open suspend fun setItemStarState(item: Item) {
        database.withTransaction {
            when {
                account.config.useSeparateState -> {
                    database.itemStateChangeDao().upsertItemStarStateChange(item, account.id, true)
                    database.itemStateDao().upsertItemStarState(
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
                    database.itemDao().updateStarState(item.id, item.isStarred)
                }

                else -> {
                    database.itemStateChangeDao().upsertItemStarStateChange(item, account.id, false)
                    database.itemDao().updateStarState(item.id, item.isStarred)
                }
            }
        }
    }

    open suspend fun setItemsRead(items: List<Item>) {
        require(items.all { it.isRead == false }) {
            "Do not add an item state change for an item which is already read"
        }

        val accountId = account.id
        val ids = items.map { it.id }

        database.withTransaction {
            when {
                account.config.useSeparateState -> {
                    items.forEach {
                        database.itemStateChangeDao().upsertItemReadStateChange(it, accountId, true)
                    }

                    database.itemStateDao().setItemsRead(
                        ids = items.map { it.remoteId!! },
                        itemStates = items.map {
                            ItemState(
                                read = true,
                                remoteId = it.remoteId!!,
                                accountId = accountId
                            )
                        },
                        accountId = accountId
                    )
                }

                account.isLocal -> {
                    database.itemDao().setAllItemsRead(ids)
                }

                else -> {
                    items.forEach {
                        database.itemStateChangeDao()
                            .upsertItemReadStateChange(it, accountId, false)
                    }
                    database.itemDao().setAllItemsRead(ids)
                }
            }
        }
    }

    open suspend fun setAllItemsRead() {
        val accountId = account.id

        database.withTransaction {
            when {
                account.config.useSeparateState -> {
                    database.itemStateChangeDao().upsertAllItemsReadStateChanges(accountId)
                    database.itemStateDao().setAllItemsRead(accountId)
                }

                account.isLocal -> {
                    database.itemDao().setAllItemsRead(account.id)
                }

                else -> {
                    database.itemStateChangeDao().upsertAllItemsReadStateChanges(accountId)
                    database.itemDao().setAllItemsRead(accountId)
                }
            }
        }
    }

    open suspend fun setAllStarredItemsRead() {
        val accountId = account.id

        database.withTransaction {
            when {
                account.config.useSeparateState -> {
                    database.itemStateChangeDao().upsertStarredItemReadStateChanges(accountId)
                    database.itemStateDao().setAllStarredItemsRead(accountId)
                }

                account.isLocal -> {
                    database.itemDao().setAllStarredItemsRead(accountId)
                }

                else -> {
                    database.itemStateChangeDao().upsertStarredItemReadStateChanges(accountId)
                    database.itemDao().setAllStarredItemsRead(accountId)
                }
            }
        }
    }

    open suspend fun setAllNewItemsRead() {
        val accountId = account.id

        database.withTransaction {
            when {
                account.config.useSeparateState -> {
                    database.itemStateChangeDao().upsertNewItemReadStateChanges(accountId)
                    database.itemStateDao().setAllNewItemsRead(accountId)
                }

                account.isLocal -> {
                    database.itemDao().setAllNewItemsRead(accountId)
                }

                else -> {
                    database.itemStateChangeDao().upsertNewItemReadStateChanges(accountId)
                    database.itemDao().setAllNewItemsRead(accountId)
                }
            }
        }
    }

    open suspend fun setAllItemsReadByFeed(feedId: Int) {
        val accountId = account.id

        database.withTransaction {
            when {
                account.config.useSeparateState -> {
                    database.itemStateChangeDao()
                        .upsertItemReadStateChangesByFeed(feedId, accountId)
                    database.itemStateDao().setAllItemsReadByFeed(feedId, accountId)
                }

                account.isLocal -> {
                    database.itemDao().setAllItemsReadByFeed(feedId, accountId)
                }

                else -> {
                    database.itemStateChangeDao()
                        .upsertItemReadStateChangesByFeed(feedId, accountId)
                    database.itemDao().setAllItemsReadByFeed(feedId, accountId)
                }
            }
        }
    }

    open suspend fun setAllItemsReadByFolder(folderId: Int) {
        val accountId = account.id

        database.withTransaction {
            when {
                account.config.useSeparateState -> {
                    database.itemStateChangeDao()
                        .upsertItemReadStateChangesByFolder(folderId, accountId)
                    database.itemStateDao().setAllItemsReadByFolder(folderId, accountId)
                }

                account.isLocal -> {
                    database.itemDao().setAllItemsReadByFolder(folderId, accountId)
                }

                else -> {
                    database.itemStateChangeDao()
                        .upsertItemReadStateChangesByFolder(folderId, accountId)
                    database.itemDao().setAllItemsReadByFolder(folderId, accountId)
                }
            }
        }
    }

    suspend fun insertOPMLFoldersAndFeeds(
        foldersAndFeeds: Map<Folder?, List<Feed>>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult {
        val errors = hashMapOf<Feed, Exception>()
        val feedsToInsert = arrayListOf<Feed>()

        for ((folder, feeds) in foldersAndFeeds) {
            if (folder != null) {
                folder.accountId = account.id

                val dbFolder = database.folderDao().selectFolderByName(folder.name!!, account.id)

                folder.id = dbFolder?.id ?: database.folderDao().insert(folder).toInt()
            }

            feeds.forEach { it.folderId = folder?.id }
            feedsToInsert += feeds
        }

        errors += insertNewFeeds(
            newFeeds = feedsToInsert,
            onUpdate = onUpdate
        )

        return errors
    }
}