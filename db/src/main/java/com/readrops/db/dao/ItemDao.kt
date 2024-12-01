package com.readrops.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.MapColumn
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
abstract class ItemDao : BaseDao<Item> {

    @Query("Select * From Item Where id = :itemId")
    abstract suspend fun select(itemId: Int): Item

    @RawQuery(observedEntities = [Item::class, Feed::class, Folder::class, ItemState::class])
    abstract fun selectAll(query: SupportSQLiteQuery): PagingSource<Int, ItemWithFeed>

    @RawQuery(observedEntities = [Item::class, ItemState::class])
    abstract fun selectItemById(query: SupportSQLiteQuery): Flow<ItemWithFeed>

    @Query("Update Item Set read = :read Where id = :itemId")
    abstract suspend fun updateReadState(itemId: Int, read: Boolean)

    @Query("Update Item Set starred = :starred Where id = :itemId")
    abstract suspend fun updateStarState(itemId: Int, starred: Boolean)

    @Query("Update Item set read = :read, starred = :starred Where remote_id = :remoteId")
    abstract suspend fun updateReadAndStarState(remoteId: String, read: Boolean, starred: Boolean)

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

    @Query("""Select count(*) From Item Inner Join Feed On Item.feed_id = Feed.id Where read = 0 
        And account_id = :accountId And DateTime(Round(Item.pub_date / 1000), 'unixepoch') 
        Between DateTime(DateTime("now"), "-24 hour") And DateTime("now")""")
    abstract fun selectUnreadNewItemsCount(accountId: Int): Flow<Int>

    @Query("""Select count(*) From ItemState Inner Join Item On Item.remote_id = ItemState.remote_id 
        Where ItemState.read = 0 and account_id = :accountId And DateTime(Round(Item.pub_date / 1000), 'unixepoch') 
        Between DateTime(DateTime("now"), "-24 hour") And DateTime("now")""")
    abstract fun selectUnreadNewItemsCountByItemState(accountId: Int): Flow<Int>

    @RawQuery(observedEntities = [Item::class, ItemState::class])
    abstract fun selectFeedUnreadItemsCount(query: SupportSQLiteQuery):
            Flow<Map<@MapColumn(columnName = "feed_id") Int, @MapColumn(columnName = "item_count") Int>>

    @Query("""Select case When Exists(Select 1 From Item Inner Join Feed on Item.feed_id = Feed.id
        Where Item.remote_id = :remoteId And account_id = :accountId) Then 1 else 0 end""")
    abstract suspend fun itemExists(remoteId: String, accountId: Int): Boolean
}