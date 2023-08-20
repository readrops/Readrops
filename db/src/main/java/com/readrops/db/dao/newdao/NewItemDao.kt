package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.pojo.ItemWithFeed
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NewItemDao : NewBaseDao<Item> {

    @RawQuery(observedEntities = [Item::class, Feed::class, Folder::class, ItemState::class])
    abstract fun selectAll(query: SupportSQLiteQuery): Flow<List<ItemWithFeed>>

    @Query("Select count(*) From Item Where feed_id = :feedId And read = 0")
    abstract fun selectUnreadCount(feedId: Int): Int

}