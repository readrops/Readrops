package com.readrops.app.repositories

import android.content.Context
import android.util.Log
import com.readrops.api.services.SyncType
import com.readrops.api.services.fever.FeverDataSource
import com.readrops.api.services.fever.FeverSyncData
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

    override fun addFeeds(results: List<ParsingResult>?): Single<List<FeedInsertionResult>> {
        TODO("Not yet implemented")
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