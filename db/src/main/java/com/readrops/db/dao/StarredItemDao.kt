package com.readrops.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.StarredItem
import com.readrops.db.pojo.ItemWithFeed

@Dao
interface StarredItemDao : BaseDao<StarredItem> {

    @Query("Delete From StarredItem Where feed_id In (Select feed_id From Feed Where account_id = :accountId)")
    fun deleteStarredItems(accountId: Int)

    @RawQuery(observedEntities = [StarredItem::class, Folder::class, Feed::class])
    fun selectAll(query: SupportSQLiteQuery?): DataSource.Factory<Int?, ItemWithFeed?>?

}