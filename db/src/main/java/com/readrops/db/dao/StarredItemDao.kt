package com.readrops.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomWarnings
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.StarredItem
import com.readrops.db.entities.UnreadItemsIds
import com.readrops.db.pojo.ItemWithFeed

@Dao
interface StarredItemDao : BaseDao<StarredItem> {

    @Query("Delete From StarredItem Where feed_id In (Select feed_id From Feed Where account_id = :accountId)")
    fun deleteStarredItems(accountId: Int)

    @RawQuery(observedEntities = [StarredItem::class, Folder::class, Feed::class, UnreadItemsIds::class])
    fun selectAll(query: SupportSQLiteQuery?): DataSource.Factory<Int, ItemWithFeed>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("Select StarredItem.id, title, StarredItem.description, content, link, pub_date, image_link, author, read, text_color, " +
            "background_color, read_time, starred, Feed.name, Feed.id as feedId, siteUrl, Folder.id as folder_id, " +
            "Folder.name as folder_name from StarredItem Inner Join Feed On StarredItem.feed_id = Feed.id Left Join Folder on Folder.id = Feed.folder_id Where StarredItem.id = :id")
    fun getStarredItemById(id: Int): LiveData<ItemWithFeed>

}