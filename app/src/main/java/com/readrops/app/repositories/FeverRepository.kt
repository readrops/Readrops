package com.readrops.app.repositories

import android.util.Log
import com.readrops.api.services.SyncType
import com.readrops.api.services.fever.FeverDataSource
import com.readrops.api.services.fever.ItemAction
import com.readrops.api.services.fever.adapters.FeverFeeds
import com.readrops.api.utils.exceptions.LoginFailedException
import com.readrops.app.util.Utils
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.ItemStateChange
import com.readrops.db.entities.account.Account

class FeverRepository(
    database: Database,
    account: Account,
    private val feverDataSource: FeverDataSource
) : BaseRepository(database, account) {

    override suspend fun login(account: Account) {
        val authenticated = feverDataSource.login(account.login!!, account.password!!)

        if (authenticated) {
            account.displayedName = account.type!!.name
        } else {
            throw LoginFailedException()
        }
    }

    override suspend fun synchronize(): SyncResult {
        val syncType = if (account.lastModified != 0L) {
            SyncType.CLASSIC_SYNC
        } else {
            SyncType.INITIAL_SYNC
        }

        return feverDataSource.synchronize(
            account.login!!,
            account.password!!,
            syncType,
            account.lastModified.toString()
        ).run {
            insertFolders(folders)
            val newFeeds = insertFeeds(feverFeeds)

            val newItems = insertItems(items)
            insertItemsIds(unreadIds, starredIds.toMutableList())

            // We use the most recent item id as lastModified instead of a timestamp
            database.accountDao().updateLastModified(sinceId, account.id)

            SyncResult(
                items = newItems,
                feeds = newFeeds,
                favicons = favicons.associateBy { favicon ->
                    val feedId = feverFeeds.favicons.entries.find { it.key == favicon.id }!!.value

                    feverFeeds.feeds.find { it.remoteId == feedId }!!
                }
            )
        }
    }

    override suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: suspend (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult> = throw NotImplementedError("This method can't be called here")

    override suspend fun insertNewFeeds(
        newFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult = throw NotImplementedError("Add feed action not supported by Fever API")

    override suspend fun updateFeed(feed: Feed) =
        throw NotImplementedError("Update feed action not supported by Fever API")

    override suspend fun deleteFeed(feed: Feed) =
        throw NotImplementedError("Delete feed action not supported by Fever API")

    override suspend fun addFolder(folder: Folder) =
        throw NotImplementedError("Add folder action not supported by Fever API")

    override suspend fun updateFolder(folder: Folder) =
        throw NotImplementedError("Update folder action not supported by Fever API")

    override suspend fun deleteFolder(folder: Folder) =
        throw NotImplementedError("Delete folder action not supported by Fever API")

    override suspend fun setItemReadState(item: Item) {
        val action = if (item.isRead) {
            ItemAction.ReadStateAction.ReadAction
        } else {
            ItemAction.ReadStateAction.UnreadAction
        }

        return setItemState(item, action)
    }

    override suspend fun setItemStarState(item: Item) {
        val action = if (item.isStarred) {
            ItemAction.StarStateAction.StarAction
        } else {
            ItemAction.StarStateAction.UnstarAction
        }

        return setItemState(item, action)
    }

    private suspend fun setItemState(item: Item, action: ItemAction) {
        try {
            val currentState = database.itemStateDao().selectItemState(account.id, item.remoteId!!)

            // if new state the same as the current one, do nothing
            if (action is ItemAction.ReadStateAction) {
                if (item.isRead == currentState.read) {
                    return
                }
            } else {
                if (item.isStarred == currentState.starred) {
                    return
                }
            }

            val itemState = ItemState(
                read = item.isRead,
                starred = item.isStarred,
                remoteId = item.remoteId!!,
                accountId = account.id,
            )

            // local state change
            if (action is ItemAction.ReadStateAction) {
                database.itemStateDao().upsertItemReadState(itemState)
            } else {
                database.itemStateDao().upsertItemStarState(itemState)
            }

            // remote state change
            feverDataSource.setItemState(
                account.login!!,
                account.password!!,
                action.value,
                item.remoteId!!
            )

            // time to process item state changes which couldn't be sent previously (no network for example)
            sendPreviousItemStateChanges()
        } catch (e: Exception) {
            // error occurred, probably network error, so we keep this change until the next state change
            if (action is ItemAction.ReadStateAction) {
                super.setItemReadState(item)
            } else {
                super.setItemStarState(item)
            }

            Log.e(TAG, "setItemStarState: ${e.message}")
        }
    }

    private suspend fun sendPreviousItemStateChanges() {
        val stateChanges = database.itemStateChangeDao()
            .selectItemStateChanges(account.id)

        for (stateChange in stateChanges) {
            val action = if (stateChange.readChange) {
                when {
                    stateChange.read -> ItemAction.ReadStateAction.ReadAction
                    else -> ItemAction.ReadStateAction.UnreadAction
                }
            } else {
                when {
                    stateChange.starred -> ItemAction.StarStateAction.StarAction
                    else -> ItemAction.StarStateAction.UnstarAction
                }
            }

            feverDataSource.setItemState(
                account.login!!,
                account.password!!,
                action.value,
                stateChange.remoteId
            )

            database.itemStateChangeDao()
                .delete(ItemStateChange(id = stateChange.id, accountId = account.id))
        }
    }

    private suspend fun insertFolders(folders: List<Folder>) {
        folders.forEach { it.accountId = account.id }
        database.folderDao().upsertFolders(folders, account)
    }

    private suspend fun insertFeeds(feverFeeds: FeverFeeds): List<Feed> = with(feverFeeds) {
        for (feed in feeds) {
            for ((folderId, feedsIds) in feedsGroups) {
                if (feedsIds.contains(feed.remoteId!!.toInt())) {
                    feed.remoteFolderId = folderId.toString()
                }
            }
        }

        feeds.forEach { it.accountId = account.id }
        return database.feedDao().upsertFeeds(feeds, account)
    }

    private suspend fun insertItems(items: List<Item>): List<Item> {
        val newItems = arrayListOf<Item>()
        val itemsFeedsIds = mutableMapOf<String?, Int>()

        for (item in items) {
            val feedId: Int
            if (itemsFeedsIds.containsKey(item.feedRemoteId)) {
                feedId = itemsFeedsIds.getValue(item.feedRemoteId)
            } else {
                feedId = database.feedDao().selectRemoteFeedLocalId(item.feedRemoteId!!, account.id)
                itemsFeedsIds[item.feedRemoteId!!] = feedId
            }

            item.feedId = feedId
            item.text?.let { item.readTime = Utils.readTimeFromString(it) }

            newItems += item
        }

        if (newItems.isNotEmpty()) {
            newItems.sortWith(Item::compareTo)
            database.itemDao().insert(newItems)
                .zip(newItems)
                .forEach { (id, item) -> item.id = id.toInt() }
        }

        return newItems
    }

    private suspend fun insertItemsIds(unreadIds: List<String>, starredIds: MutableList<String>) {
        database.itemStateDao().deleteItemStates(account.id)

        database.itemStateDao().insert(unreadIds.map { unreadId ->
            val starred = starredIds.any { starredId -> starredId == unreadId }
            if (starred) {
                starredIds.remove(unreadId)
            }

            ItemState(
                id = 0,
                read = false,
                starred = starred,
                remoteId = unreadId,
                accountId = account.id,
            )
        })

        if (starredIds.isNotEmpty()) {
            database.itemStateDao().insert(starredIds.map { starredId ->
                ItemState(
                    id = 0,
                    read = true, // if this id wasn't in the unread ids list, it is considered a read
                    starred = true,
                    remoteId = starredId,
                    accountId = account.id,
                )
            })
        }
    }

    companion object {
        val TAG: String = FeverRepository::class.java.simpleName
    }
}
