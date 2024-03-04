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

    @Query("Update Item set read = 1 Where feed_id IN (Select id From Feed Where account_id = :accountId)")
    abstract suspend fun setAllItemsRead(accountId: Int)

    @Query("Update Item set read = 1 Where starred = 1 And feed_id IN (Select id From Feed Where account_id = :accountId)")
    abstract suspend fun setAllStarredItemsRead(accountId: Int)

    @Query("Update Item set read = 1 Where DateTime(Round(pub_date / 1000), 'unixepoch') " +
            "Between DateTime(DateTime(\"now\"), \"-24 hour\") And DateTime(\"now\") " +
            "And feed_id IN (Select id From Feed Where account_id = :accountId)")
    abstract suspend fun setAllNewItemsRead(accountId: Int)

    @Query("Update Item set read = 1 Where feed_id IN " +
            "(Select id From Feed Where id = :feedId And account_id = :accountId)")
    abstract suspend fun setAllItemsReadByFeed(feedId: Int, accountId: Int)

    @Query("Update Item set read = 1 Where feed_id IN (Select Feed.id From Feed Inner Join Folder " +
            "On Feed.folder_id = Folder.id Where Folder.id = :folderId And Folder.account_id = :accountId)")
    abstract suspend fun setAllItemsReadByFolder(folderId: Int, accountId: Int)
}