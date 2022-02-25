package com.readrops.app.repositories

import android.content.Context
import android.util.Log
import com.readrops.api.services.SyncType
import com.readrops.api.services.fever.FeverDataSource
import com.readrops.api.services.fever.FeverSyncData
import com.readrops.api.services.fever.ItemAction
import com.readrops.api.services.fever.adapters.FeverFeeds
import com.readrops.api.utils.ApiUtils
import com.readrops.app.addfeed.FeedInsertionResult
import com.readrops.app.addfeed.ParsingResult
import com.readrops.app.utils.Utils
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.account.Account
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import okhttp3.MultipartBody

class FeverRepository(
        private val feverDataSource: FeverDataSource,
        private val dispatcher: CoroutineDispatcher,
        database: Database,
        context: Context,
        account: Account?,
) : ARepository(database, context, account) {

    override fun login(account: Account, insert: Boolean): Completable =
            rxCompletable(context = dispatcher) {
                try {
                    feverDataSource.login(getFeverRequestBody())
                    account.displayedName = account.accountType!!.name

                    database.accountDao().insert(account)
                            .doOnSuccess { account.id = it.toInt() }
                            .await()
                } catch (e: Exception) {
                    Log.e(TAG, "login: ${e.message}")
                    error(e.message!!)
                }
            }

    override fun sync(feeds: List<Feed>?, update: FeedUpdate?): Completable =
            rxCompletable(context = dispatcher) {
                try {
                    val syncType = if (account.lastModified != 0L) {
                        SyncType.CLASSIC_SYNC
                    } else {
                        SyncType.INITIAL_SYNC
                    }

                    val syncResult = feverDataSource.sync(syncType,
                            FeverSyncData(account.lastModified.toString()), getFeverRequestBody())

                    insertFolders(syncResult.folders)
                    insertFeeds(syncResult.feverFeeds)

                    insertItems(syncResult.items)
                    insertItemsIds(syncResult.unreadIds, syncResult.starredIds.toMutableList())

                    // We store the id to use for the next synchronisation even if it's not a timestamp
                    database.accountDao().updateLastModified(account.id, syncResult.sinceId)
                } catch (e: Exception) {
                    Log.e(TAG, "sync: ${e.message}")
                    error(e.message!!)
                }
            }

    // Not supported by Fever API
    override fun addFeeds(results: List<ParsingResult>?): Single<List<FeedInsertionResult>> = Single.just(listOf())

    // Not supported by Fever API
    override fun updateFeed(feed: Feed?): Completable = Completable.complete()

    // Not supported by Fever API
    override fun deleteFeed(feed: Feed?): Completable = Completable.complete()

    // Not supported by Fever API
    override fun addFolder(folder: Folder?): Single<Long> = Single.just(0)

    // Not supported by Fever API
    override fun updateFolder(folder: Folder?): Completable = Completable.complete()

    // Not supported by Fever API
    override fun deleteFolder(folder: Folder?): Completable = Completable.complete()

    override fun setItemReadState(item: Item): Completable {
        val action = if (item.isRead) ItemAction.ReadStateAction.ReadAction else ItemAction.ReadStateAction.UnreadAction
        return setItemState(item, action)
    }

    override fun setItemStarState(item: Item): Completable {
        val action = if (item.isStarred) ItemAction.StarStateAction.StarAction else ItemAction.StarStateAction.UnstarAction
        return setItemState(item, action)
    }

    private fun setItemState(item: Item, action: ItemAction): Completable = rxCompletable(context = dispatcher) {
        try {
            feverDataSource.setItemState(getFeverRequestBody(), action.value, item.remoteId!!)
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

            completable.await()
        } catch (e: Exception) {
            val completable = if (action is ItemAction.ReadStateAction) {
                super.setItemReadState(item)
            } else {
                super.setItemStarState(item)
            }

            completable.await()
            Log.e(TAG, "setItemStarState: ${e.message}")
            error(e.message!!)
        }
    }

    private suspend fun sendPreviousItemStateChanges() {
        val stateChanges = database.itemStateChangesDao().getItemStateChanges(account.id)

        for (stateChange in stateChanges) {
            val action = if (stateChange.readChange) {
                if (stateChange.read) ItemAction.ReadStateAction.ReadAction else ItemAction.ReadStateAction.UnreadAction
            } else { // star change
                if (stateChange.starred) ItemAction.StarStateAction.StarAction else ItemAction.StarStateAction.UnstarAction
            }

            feverDataSource.setItemState(getFeverRequestBody(), action.value, stateChange.remoteId)
        }
    }

    private fun insertFolders(folders: List<Folder>) {
        folders.forEach { it.accountId = account.id }
        database.folderDao().foldersUpsert(folders, account)
    }

    private fun insertFeeds(feverFeeds: FeverFeeds) = with(feverFeeds) {
        for (feed in feeds) {
            for ((folderId, feedsIds) in feedsGroups) {
                if (feedsIds.contains(feed.remoteId!!.toInt())) feed.remoteFolderId = folderId.toString()
            }
        }

        feeds.forEach { it.accountId = account.id }
        database.feedDao().feedsUpsert(feeds, account)
    }

    private fun insertItems(items: List<Item>) {
        val itemsToInsert = arrayListOf<Item>()
        val itemsFeedsIds = mutableMapOf<String, Int>()

        for (item in items) {
            var feedId: Int?
            if (itemsFeedsIds.containsKey(item.feedRemoteId)) {
                feedId = itemsFeedsIds[item.feedRemoteId]
            } else {
                feedId = database.feedDao().getFeedIdByRemoteId(item.feedRemoteId!!, account.id)
                itemsFeedsIds[item.feedRemoteId!!] = feedId
            }

            item.feedId = feedId!!
            item.text?.let { item.readTime = Utils.readTimeFromString(it) }

            itemsToInsert += item
        }

        if (itemsToInsert.isNotEmpty()) {
            itemsToInsert.sortWith(Item::compareTo)
            database.itemDao().insert(itemsToInsert)
        }
    }

    private fun insertItemsIds(unreadIds: List<String>, starredIds: MutableList<String>) {
        database.itemStateDao().deleteItemsStates(account.id)

        database.itemStateDao().insertItemStates(unreadIds.map { unreadId ->
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
            database.itemStateDao().insertItemStates(starredIds.map { starredId ->
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