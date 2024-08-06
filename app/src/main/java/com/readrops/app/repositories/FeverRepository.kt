package com.readrops.app.repositories

import android.util.Log
import com.readrops.api.services.SyncType
import com.readrops.api.services.fever.FeverDataSource
import com.readrops.api.services.fever.FeverSyncData
import com.readrops.api.services.fever.ItemAction
import com.readrops.api.services.fever.adapters.FeverFeeds
import com.readrops.api.utils.ApiUtils
import com.readrops.api.utils.exceptions.LoginFailedException
import com.readrops.app.util.Utils
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.account.Account
import okhttp3.MultipartBody

class FeverRepository(
    database: Database,
    account: Account,
    private val feverDataSource: FeverDataSource
) : BaseRepository(database, account) {

    override suspend fun login(account: Account) {
        val authenticated = feverDataSource.login(account.login!!, account.password!!)

        if (authenticated) {
            account.displayedName = account.accountType!!.name
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
            syncType,
            FeverSyncData(account.lastModified.toString()),
            getFeverRequestBody()
        ).run {
            insertFolders(folders)
            insertFeeds(feverFeeds)

            insertItems(items)
            insertItemsIds(unreadIds, starredIds.toMutableList())

            // We store the id to use for the next synchronisation even if it's not a timestamp
            database.accountDao().updateLastModified(sinceId, account.id)

            SyncResult()
        }
    }

    override suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: suspend (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult> = throw NotImplementedError("This method can't be called here")

    // Not supported by Fever API
    override suspend fun insertNewFeeds(
        newFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult = throw CloneNotSupportedException()

    // Not supported by Fever API
    override suspend fun updateFeed(feed: Feed) {}

    // Not supported by Fever API
    override suspend fun deleteFeed(feed: Feed) {}

    // Not supported by Fever API
    override suspend fun addFolder(folder: Folder) {}

    // Not supported by Fever API
    override suspend fun updateFolder(folder: Folder) {}

    // Not supported by Fever API
    override suspend fun deleteFolder(folder: Folder) {}

    override suspend fun setItemReadState(item: Item) {
        val action =
            if (item.isRead) ItemAction.ReadStateAction.ReadAction else ItemAction.ReadStateAction.UnreadAction
        return setItemState(item, action)
    }

    override suspend fun setItemStarState(item: Item) {
        val action =
            if (item.isStarred) ItemAction.StarStateAction.StarAction else ItemAction.StarStateAction.UnstarAction
        return setItemState(item, action)
    }

    private suspend fun setItemState(item: Item, action: ItemAction) {
        try {
            feverDataSource.setItemState(account.login!!, account.password!!, action.value, item.remoteId!!)
            val itemState = ItemState(
                read = item.isRead,
                starred = item.isStarred,
                remoteId = item.remoteId!!,
                accountId = account.id,
            )

            val completable = if (action is ItemAction.ReadStateAction) {
                database.itemStateDao().upsertItemReadState(itemState)
            } else {
                database.itemStateDao().upsertItemStarState(itemState)
            }

        } catch (e: Exception) {
            val completable = if (action is ItemAction.ReadStateAction) {
                super.setItemReadState(item)
            } else {
                super.setItemStarState(item)
            }

            Log.e(TAG, "setItemStarState: ${e.message}")
            error(e.message!!)
        }
    }

    private suspend fun sendPreviousItemStateChanges() {
        val stateChanges = database.itemStateChangeDao()
            .selectItemStateChanges(account.id)

        for (stateChange in stateChanges) {
            val action = if (stateChange.readChange) {
                if (stateChange.read) ItemAction.ReadStateAction.ReadAction else ItemAction.ReadStateAction.UnreadAction
            } else { // star change
                if (stateChange.starred) ItemAction.StarStateAction.StarAction else ItemAction.StarStateAction.UnstarAction
            }

            feverDataSource.setItemState(account.login!!, account.password!!, action.value, stateChange.remoteId)
        }
    }

    private suspend fun insertFolders(folders: List<Folder>) {
        folders.forEach { it.accountId = account.id }
        database.folderDao().upsertFolders(folders, account)
    }

    private suspend fun insertFeeds(feverFeeds: FeverFeeds) = with(feverFeeds) {
        for (feed in feeds) {
            for ((folderId, feedsIds) in feedsGroups) {
                if (feedsIds.contains(feed.remoteId!!.toInt())) {
                    feed.remoteFolderId = folderId.toString()
                }
            }
        }

        feeds.forEach { it.accountId = account.id }
        database.feedDao().upsertFeeds(feeds, account)
    }

    private suspend fun insertItems(items: List<Item>): List<Item> {
        val newItems = arrayListOf<Item>()
        val itemsFeedsIds = mutableMapOf<String, Int>()

        for (item in items) {
            var feedId: Int?
            if (itemsFeedsIds.containsKey(item.feedRemoteId)) {
                feedId = itemsFeedsIds[item.feedRemoteId]
            } else {
                //feedId = database.feedDao().getFeedIdByRemoteId(item.feedRemoteId!!, account.id)
               // itemsFeedsIds[item.feedRemoteId!!] = feedId
            }

            //item.feedId = feedId!!
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
            if (starred) starredIds.remove(unreadId)

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

    private fun getFeverRequestBody(): MultipartBody {
        val credentials = ApiUtils.md5hash("${account.login}:${account.password}")
        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_key", credentials)
            .build()
    }

    companion object {
        val TAG: String = FeverRepository::class.java.simpleName
    }
}
