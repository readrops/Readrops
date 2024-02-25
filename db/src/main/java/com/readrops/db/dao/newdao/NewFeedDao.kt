package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Feed
import com.readrops.db.pojo.FeedWithCount
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NewFeedDao : NewBaseDao<Feed> {

    @Query("Select * From Feed")
    abstract fun selectFeeds(): Flow<List<Feed>>

    @Query("Select * from Feed Where account_id = :accountId order by name ASC")
    abstract suspend fun selectFeeds(accountId: Int): List<Feed>

    @Query("Update Feed set etag = :etag, last_modified = :lastModified Where id = :feedId")
    abstract suspend fun updateHeaders(etag: String, lastModified: String, feedId: Int)

    @Query("Select case When :feedUrl In (Select url from Feed Where account_id = :accountId) Then 1 else 0 end")
    abstract suspend fun feedExists(feedUrl: String, accountId: Int): Boolean

    @Query("With main As (Select Feed.id as feedId, Feed.name as feedName, Feed.icon_url as feedIcon, " +
            "Feed.url as feedUrl, Feed.siteUrl as feedSiteUrl, Feed.account_id as accountId, Item.read as itemRead " +
            "From Feed Left Join Item On Feed.id = Item.feed_id Where Feed.folder_id is Null And Feed.account_id = :accountId)" +
            "Select feedId, feedName, feedIcon, feedUrl, feedSiteUrl, accountId, " +
            "(Select count(*) From main Where (itemRead = 0)) as unreadCount From main Group by feedId Order By feedName")
    abstract fun selectFeedsWithoutFolder(accountId: Int): Flow<List<FeedWithCount>>

    @Query("Update Feed set name = :feedName, url = :feedUrl, folder_id = :folderId Where id = :feedId")
    abstract fun updateFeedFields(feedId: Int, feedName: String, feedUrl: String, folderId: Int?)

}