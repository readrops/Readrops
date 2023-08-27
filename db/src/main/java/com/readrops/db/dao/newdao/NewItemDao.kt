package com.readrops.db.dao.newdao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.pojo.ItemWithFeed

@Dao
abstract class NewItemDao : NewBaseDao<Item> {

    @RawQuery(observedEntities = [Item::class, Feed::class, Folder::class, ItemState::class])
    abstract fun selectAll(query: SupportSQLiteQuery): PagingSource<Int, ItemWithFeed>

    @Query("Update Item Set read = :read Where id = :itemId")
    abstract suspend fun updateReadState(itemId: Int, read: Boolean)

    @Query("Update Item Set starred = :starred Where id = :itemId")
    abstract suspend fun updateStarState(itemId: Int, starred: Boolean)
}