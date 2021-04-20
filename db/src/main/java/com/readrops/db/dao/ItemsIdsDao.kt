package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.readrops.db.entities.ItemStateId
import com.readrops.db.entities.ReadStarStateChange
import com.readrops.db.entities.UnreadItemsIds
import com.readrops.db.pojo.ItemReadStarState
import io.reactivex.Completable

@Dao
interface ItemsIdsDao {

    @Insert
    fun insertUnreadItemsIds(unreadItemsIds: List<UnreadItemsIds>)

    @Insert
    fun insertUnreadItemId(unreadItemId: UnreadItemsIds)

    @Query("Delete From UnreadItemsIds Where remote_id = :remoteId And account_id = :accountId")
    fun deleteUnreadItemId(remoteId: String, accountId: Int)

    fun upsertUnreadItemId(unreadItemId: UnreadItemsIds) = Completable.create {
        if (unreadItemIdExists(unreadItemId.remoteId, unreadItemId.accountId)) {
            deleteUnreadItemId(unreadItemId.remoteId, unreadItemId.accountId)
        } else {
            insertUnreadItemId(unreadItemId)
        }

        it.onComplete()
    }

    @Query("Select case When Exists (Select remote_id, account_id From UnreadItemsIds Where remote_id = :remoteId And account_id = :accountId) Then 1 else 0 End")
    fun unreadItemIdExists(remoteId: String, accountId: Int): Boolean

    @Insert
    fun insertReadStarStateChange(readStarStateChange: ReadStarStateChange)

    @Delete
    fun deleteReadStarStateChange(readStarStateChange: ReadStarStateChange)

    @Query("Delete From UnreadItemsIds Where account_id = :accountId")
    fun deleteUnreadItemsIds(accountId: Int)

    @Query("Delete From ReadStarStateChange Where account_id = :accountId")
    fun deleteReadStarStateChanges(accountId: Int)

    @Query("Delete From ReadStarStateChange Where account_id = :accountId")
    fun deleteStateChanges(accountId: Int)

    @Query("Select case When UnreadItemsIds.remote_id is NULL Then 1 else 0 End read, Item.remoteId, ReadStarStateChange.read_change, Item.starred, ReadStarStateChange.star_change " +
            "From ReadStarStateChange Inner Join Item On ReadStarStateChange.id = Item.id " +
            "Left Join UnreadItemsIds On UnreadItemsIds.remote_id = Item.remoteId Where ReadStarStateChange.account_id = :accountId")
    fun getItemStateChanges(accountId: Int): List<ItemReadStarState>

    @Query("Select StarredItem.remoteId, Case When StarredItem.read = 1 then 0 else 1 end read, StarredItem.starred, ReadStarStateChange.read_change, " +
            "ReadStarStateChange.star_change From StarredItem Inner Join ReadStarStateChange On StarredItem.id = ReadStarStateChange.id Where account_id = :accountId")
    fun getStarredItemStateChanges(accountId: Int): List<ItemReadStarState>

    fun upsertReadStarStateChange(readStarStateChange: ReadStarStateChange) = Completable.create {
        if (readStarStateChange.readChange && readStateChangeExists(readStarStateChange.id) ||
                readStarStateChange.starChange && starStateChangeExists(readStarStateChange.id)) {
            deleteReadStarStateChange(readStarStateChange)
        } else {
            insertReadStarStateChange(readStarStateChange)
        }

        it.onComplete()
    }

    @Query("Select Case When :itemId In (Select id From ReadStarStateChange Where read_change = 1) Then 1 Else 0 End")
    fun readStateChangeExists(itemId: Int): Boolean

    @Query("Select Case When :itemId In (Select id From ReadStarStateChange Where star_change = 1) Then 1 Else 0 End")
    fun starStateChangeExists(itemId: Int): Boolean

    @Query("Delete From ItemStateId Where account_id = :accountId")
    fun deleteItemsIds(accountId: Int)

    @Insert
    fun insertItemsIds(itemsIds: List<ItemStateId>)
}