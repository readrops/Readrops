package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.readrops.db.pojo.FeedWithCount
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

}