package com.readrops.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.FeedWithFolder
import io.reactivex.Completable
import io.reactivex.Single
import java.lang.Exception
import java.util.ArrayList

@Dao
abstract class FeedDao : BaseDao<Feed> {

    @Query("Select * from Feed Where account_id = :accountId order by name ASC")
    abstract fun getFeeds(accountId: Int): List<Feed>

    @get:Query("Select * from Feed Order By name ASC")
    abstract val allFeeds: LiveData<List<Feed>>

    @Query("Select * from Feed Where id = :feedId")
    abstract fun getFeedById(feedId: Int): Feed

    @Query("Select case When :feedUrl In (Select url from Feed Where account_id = :accountId) Then 1 else 0 end")
    abstract fun feedExists(feedUrl: String, accountId: Int): Boolean

    @Query("Select case When :remoteId In (Select remoteId from Feed Where account_id = :accountId) Then 1 else 0 end")
    abstract fun remoteFeedExists(remoteId: String, accountId: Int): Boolean

    @Query("Select count(*) from Feed Where account_id = :accountId")
    abstract fun getFeedCount(accountId: Int): Single<Int>

    @Query("Select * from Feed Where url = :feedUrl And account_id = :accountId")
    abstract fun getFeedByUrl(feedUrl: String, accountId: Int): Feed

    @Query("Select id from Feed Where remoteId = :remoteId And account_id = :accountId")
    abstract fun getFeedIdByRemoteId(remoteId: String, accountId: Int): Int

    @Query("Select * from Feed Where folder_id = :folderId")
    abstract fun getFeedsByFolder(folderId: Int): List<Feed>

    @Query("Select * from Feed Where account_id = :accountId And folder_id is null")
    abstract fun getFeedsWithoutFolder(accountId: Int): List<Feed>

    @Query("Update Feed set etag = :etag, last_modified = :lastModified Where id = :feedId")
    abstract fun updateHeaders(etag: String, lastModified: String, feedId: Int)

    @Query("Update Feed set name = :feedName, url = :feedUrl, folder_id = :folderId Where id = :feedId")
    abstract fun updateFeedFields(feedId: Int, feedName: String, feedUrl: String, folderId: Int)

    @Query("Update Feed set name = :name, folder_id = :folderId Where remoteId = :remoteFeedId And account_id = :accountId")
    abstract fun updateNameAndFolder(remoteFeedId: String, accountId: Int, name: String, folderId: Int?)

    @Query("Update Feed set text_color = :textColor, background_color = :bgColor Where id = :feedId")
    abstract fun updateColors(feedId: Int, textColor: Int, bgColor: Int)

    @Query("Select Feed.name as feed_name, Feed.id as feed_id, Folder.name as folder_name, Folder.id as folder_id, Folder.remoteId as folder_remoteId, Folder.account_id as folder_account_id," +
            "Feed.description as feed_description, Feed.icon_url as feed_icon_url, Feed.url as feed_url, Feed.folder_id as feed_folder_id, Feed.text_color as feed_text_color, Feed.background_color as feed_background_color" +
            ", Feed.account_id as feed_account_id, Feed.notification_enabled as feed_notification_enabled, Feed.siteUrl as feed_siteUrl, Feed.remoteId as feed_remoteId from Feed Left Join Folder on Feed.folder_id = Folder.id Where Feed.account_id = :accountId Order by Feed.name")
    abstract fun getAllFeedsWithFolder(accountId: Int): LiveData<List<FeedWithFolder>>

    @Query("Select id, name, icon_url, notification_enabled, text_color, background_color, account_id From Feed Where account_id = :accountId")
    abstract fun getFeedsForNotifPermission(accountId: Int): LiveData<List<Feed>>

    @Query("Select * From Feed Where id in (:ids)")
    abstract fun selectFromIdList(ids: List<Long>): List<Feed>

    @Query("Select remoteId From Feed Where account_id = :accountId")
    abstract fun getFeedRemoteIdsOfAccount(accountId: Int): MutableList<String>

    @Query("Delete from Feed Where remoteId in (:ids) And account_id = :accountId")
    abstract fun deleteByIds(ids: List<String>, accountId: Int)

    @Query("Select id From Folder Where remoteId = :remoteId And account_id = :accountId")
    abstract fun getRemoteFolderLocalId(remoteId: String, accountId: Int): Int

    @Query("Update Feed set notification_enabled = :enabled Where id = :feedId")
    abstract fun updateFeedNotificationState(feedId: Int, enabled: Boolean): Completable

    @Query("Update Feed set notification_enabled = :enabled Where account_id = :accountId")
    abstract fun updateAllFeedsNotificationState(accountId: Int, enabled: Boolean): Completable

    /**
     * Insert, update and delete feeds, by account
     *
     * @param feeds   feeds to insert or update
     * @param account owner of the feeds
     * @return the list of the inserted feeds ids
     */
    @Transaction
    open fun feedsUpsert(feeds: List<Feed>, account: Account): List<Long> {
        val accountFeedIds = getFeedRemoteIdsOfAccount(account.id)
        val feedsToInsert = arrayListOf<Feed>()

        for (feed in feeds) {
            val folderId: Int? = try {
                val remoteFolderId = feed.remoteFolderId!!.toInt()
                if (remoteFolderId == 0) null else getRemoteFolderLocalId(feed.remoteFolderId!!, account.id)
            } catch (e: Exception) {
                if (feed.remoteFolderId == null) null else getRemoteFolderLocalId(feed.remoteFolderId!!, account.id)
            }

            if (remoteFeedExists(feed.remoteId!!, account.id)) {
                updateNameAndFolder(feed.remoteId!!, account.id, feed.name!!, folderId)
                accountFeedIds.remove(feed.remoteId)
            } else {
                feed.folderId = folderId
                feedsToInsert.add(feed)
            }
        }

        if (accountFeedIds.isNotEmpty())
            deleteByIds(accountFeedIds, account.id)

        return insert(feedsToInsert)
    }
}