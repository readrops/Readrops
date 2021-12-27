package com.readrops.app.repositories

import android.content.Context
import com.readrops.api.services.SyncType
import com.readrops.api.services.fever.FeverDataSource
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

    override fun login(account: Account, insert: Boolean): Completable {
        return rxCompletable(context = dispatcher) {
            try {
                feverDataSource.login(account.login!!, account.password!!)
                account.displayedName = account.accountType!!.name

                database.accountDao().insert(account)
                        .doOnSuccess { account.id = it.toInt() }
                        .await()
            } catch (e: Exception) {
                error(e.message!!)
            }
        }
    }

    override fun sync(feeds: List<Feed>?, update: FeedUpdate?): Completable {
        return rxCompletable(context = dispatcher) {
            try {
                val syncType = if (account.lastModified != 0L) {
                    SyncType.CLASSIC_SYNC
                } else {
                    SyncType.INITIAL_SYNC
                }

                val credentials = ApiUtils.md5hash("${account.login}:${account.password}")

                val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("api_key", credentials)
                        .build()

                val syncResult = feverDataSource.sync(requestBody)

                insertFolders(syncResult.folders)
                insertFeeds(syncResult.feverFeeds)

                //insertItems(syncResult.items)
                //insertItemsIds(syncResult.unreadIds!!, syncResult.starredIds!!.toMutableList())
            } catch (e: Exception) {
                error(e.message!!)
            }
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

    }

    private fun insertItemsIds(unreadIds: List<String>, starredIds: List<String>) {

    }
}