package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Feed
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
}