package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.FeedWithCount
import com.readrops.db.pojo.FeedWithFolder2
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NewFeedDao : NewBaseDao<Feed> {

    @Query("Select * From Feed Where id = :feedId")
    abstract suspend fun selectFeed(feedId: Int): Feed

    @Query("Select * From Feed Where folder_id = :folderId")
    abstract suspend fun selectFeedsByFolder(folderId: Int): List<Feed>

    @Query("Select * from Feed Where account_id = :accountId order by name ASC")
    abstract suspend fun selectFeeds(accountId: Int): List<Feed>

    @Query("Update Feed set etag = :etag, last_modified = :lastModified Where id = :feedId")
    abstract suspend fun updateHeaders(etag: String, lastModified: String, feedId: Int)

    @Query("Select case When :feedUrl In (Select url from Feed Where account_id = :accountId) Then 1 else 0 end")
    abstract suspend fun feedExists(feedUrl: String, accountId: Int): Boolean

    @RawQuery(observedEntities = [Feed::class, Item::class])
    abstract fun selectFeedsWithoutFolder(query: SupportSQLiteQuery): Flow<List<FeedWithCount>>

    @Query("Update Feed set name = :feedName, url = :feedUrl, folder_id = :folderId Where id = :feedId")
    abstract fun updateFeedFields(feedId: Int, feedName: String, feedUrl: String, folderId: Int?)

    @Query("Select count(*) from Feed Where account_id = :accountId")
    abstract suspend fun selectFeedCount(accountId: Int): Int

    @Query("Select remoteId From Feed Where account_id = :accountId")
    abstract suspend fun selectFeedRemoteIds(accountId: Int): MutableList<String>

    @Query("Select id From Folder Where remoteId = :remoteId And account_id = :accountId")
    abstract suspend fun selectRemoteFolderLocalId(remoteId: String, accountId: Int): Int

    @Query("Select id From Feed Where remoteId = :remoteId And account_id = :accountId")
    abstract suspend fun selectRemoteFeedLocalId(remoteId: String, accountId: Int): Int

    @Query("Update Feed set name = :name, folder_id = :folderId Where remoteId = :remoteFeedId And account_id = :accountId")
    abstract fun updateFeedNameAndFolder(remoteFeedId: String, accountId: Int, name: String, folderId: Int?)

    @Query("Delete from Feed Where remoteId in (:ids) And account_id = :accountId")
    abstract fun deleteByIds(ids: List<String>, accountId: Int)

    @Query("Update Feed set background_color = :color Where id = :feedId")
    abstract fun updateFeedColor(feedId: Int, color: Int)

    @Query("""Select Feed.*, Folder.name as folder_name From Feed Left Join Folder On Feed.folder_id = Folder.id 
        Where Feed.account_id = :accountId Order By Feed.name, Folder.name""")
    abstract fun selectFeedsWithFolderName(accountId: Int): Flow<List<FeedWithFolder2>>

    @Query("Update Feed set notification_enabled = :enabled Where id = :feedId")
    abstract suspend fun updateFeedNotificationState(feedId: Int, enabled: Boolean)

    @Query("Update Feed set notification_enabled = :enabled Where account_id = :accountId")
    abstract suspend fun updateAllFeedsNotificationState(accountId: Int, enabled: Boolean)

    /**
     * Insert, update and delete feeds by account
     *
     * @param feeds   feeds to insert or update
     * @param account owner of the feeds
     * @return the list of the inserted feeds ids
     */
    @Transaction
    open suspend fun upsertFeeds(feeds: List<Feed>, account: Account): List<Long> {
        val localFeedIds = selectFeedRemoteIds(account.id)

        val feedsToInsert = feeds.filter { feed -> localFeedIds.none { localFeedId -> feed.remoteId == localFeedId } }
        val feedsToDelete = localFeedIds.filter { localFeedId -> feeds.none { feed -> localFeedId == feed.remoteId } }

        feeds.forEach { feed ->
            feed.folderId = if (feed.remoteFolderId == null) {
                null
            } else {
                selectRemoteFolderLocalId(feed.remoteFolderId!!, account.id)
            }

            // works only for already existing feeds
            updateFeedNameAndFolder(feed.remoteId!!, account.id, feed.name!!, feed.folderId)
        }

        if (feedsToDelete.isNotEmpty()) {
            deleteByIds(feedsToDelete, account.id)
        }

        return if (feedsToInsert.isNotEmpty()) {
            insert(feedsToInsert).apply {
                feedsToInsert.zip(this).forEach { (feed, id) -> feed.id = id.toInt() }
            }
        } else listOf()
    }
}