package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.readrops.db.entities.ItemStateChange
import com.readrops.db.pojo.ItemReadStarState
import io.reactivex.Completable

@Dao
interface ItemStateChangeDao : BaseDao<ItemStateChange> {

    @Insert
    fun insertItemStateChange(itemStateChange: ItemStateChange)

    @Delete
    fun deleteItemStateChange(itemStateChange: ItemStateChange)

    @Query("Delete From ItemStateChange Where account_id = :accountId")
    fun resetStateChanges(accountId: Int)

    @Query("Select case When ItemState.remote_id is NULL Or ItemState.read = 1 Then 1 else 0 End read,  " +
            "case When ItemState.remote_id is NULL Or ItemState.starred = 1 Then 1 else 0 End starred," +
            "ItemStateChange.read_change, ItemStateChange.star_change, Item.remoteId " +
            "From ItemStateChange Inner Join Item On ItemStateChange.id = Item.id " +
            "Left Join ItemState On ItemState.remote_id = Item.remoteId Where ItemStateChange.account_id = :accountId")
    fun getItemStateChanges(accountId: Int): List<ItemReadStarState>

    fun upsertItemStateChange(itemStateChange: ItemStateChange) = Completable.create {
        if (itemStateChange.readChange && readStateChangeExists(itemStateChange.id) ||
                itemStateChange.starChange && starStateChangeExists(itemStateChange.id)) {
            deleteItemStateChange(itemStateChange)
        } else {
            insertItemStateChange(itemStateChange)
        }

        it.onComplete()
    }

    @Query("Select Case When :itemId In (Select id From ItemStateChange Where read_change = 1) Then 1 Else 0 End")
    fun readStateChangeExists(itemId: Int): Boolean

    @Query("Select Case When :itemId In (Select id From ItemStateChange Where star_change = 1) Then 1 Else 0 End")
    fun starStateChangeExists(itemId: Int): Boolean
}