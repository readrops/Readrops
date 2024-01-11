package com.readrops.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.pojo.StarItem
import io.reactivex.Completable

@Dao
interface ItemDao : BaseDao<Item> {

    @RawQuery(observedEntities = [Item::class, Folder::class, Feed::class, ItemState::class])
    fun selectAll(query: SupportSQLiteQuery): DataSource.Factory<Int, ItemWithFeed>

    @Query("Select * From Item Where id = :itemId")
    fun select(itemId: Int): Item

    @Query("Select case When :guid In (Select guid From Item Inner Join Feed on Item.feed_id = Feed.id and account_id = :accountId) Then 1 else 0 end")
    fun itemExists(guid: String, accountId: Int): Boolean

    @Query("Select case When :remoteId In (Select remoteId from Item) And :feedId In (Select feed_id From Item) Then 1 else 0 end")
    fun remoteItemExists(remoteId: String, feedId: Int): Boolean

    @Query("Select * From Item Where remoteId = :remoteId And feed_id = :feedId")
    fun selectByRemoteId(remoteId: String, feedId: Int): Item

    @Query("Update Item Set read = :read Where id = :itemId")
    fun setReadState(itemId: Int, read: Boolean): Completable

    @Query("Update Item set starred = :starred Where id = :itemId")
    fun setStarState(itemId: Int, starred: Boolean): Completable

    @Query("Update Item set read = :readState Where feed_id In (Select id From Feed Where account_id = :accountId)")
    fun setAllItemsReadState(readState: Int, accountId: Int): Completable

    @Query("Update Item set read = :readState Where feed_id = :feedId")
    fun setAllFeedItemsReadState(feedId: Int, readState: Int): Completable

    @Query("Update Item set read_it_later = :readLater Where id = :itemId")
    fun setReadItLater(readLater: Boolean, itemId: Int): Completable

    @Query("Select count(*) From Item Where feed_id = :feedId And read = 0")
    fun getUnreadCount(feedId: Int): Int

    @RawQuery(observedEntities = [Item::class, ItemState::class])
    fun getItemById(query: SupportSQLiteQuery): LiveData<ItemWithFeed>

    @Query("Select Item.guid, Feed.remoteId as feedRemoteId From Item Inner Join Feed On Item.feed_id = Feed.id Where Item.remoteId In (:remoteIds) And account_id = :accountId")
    fun getStarChanges(remoteIds: List<String>, accountId: Int): List<StarItem>

    @Query("Update Item set read = :read, starred = :starred Where remoteId = :remoteId")
    fun setReadAndStarState(remoteId: String, read: Boolean, starred: Boolean)
}