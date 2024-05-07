package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemStateChange
import com.readrops.db.pojo.ItemReadStarState

@Dao
interface NewItemStateChangeDao: NewBaseDao<ItemStateChange> {

    @Query("Delete From ItemStateChange Where account_id = :accountId")
    suspend fun resetStateChanges(accountId: Int)

    @Query("Select case When ItemState.remote_id is NULL Or ItemState.read = 1 Then 1 else 0 End read,  " +
            "case When ItemState.remote_id is NULL Or ItemState.starred = 1 Then 1 else 0 End starred," +
            "ItemStateChange.read_change, ItemStateChange.star_change, Item.remoteId " +
            "From ItemStateChange Inner Join Item On ItemStateChange.id = Item.id " +
            "Left Join ItemState On ItemState.remote_id = Item.remoteId Where ItemStateChange.account_id = :accountId")
    suspend fun getItemStateChanges(accountId: Int): List<ItemReadStarState>

    @Query("Select Item.read, Item.starred," +
            "ItemStateChange.read_change, ItemStateChange.star_change, Item.remoteId " +
            "From ItemStateChange Inner Join Item On ItemStateChange.id = Item.id " +
            "Where ItemStateChange.account_id = :accountId")
    suspend fun getNextcloudNewsStateChanges(accountId: Int): List<ItemReadStarState>

    @Query("Select Case When :itemId In (Select id From ItemStateChange Where read_change = 1) Then 1 Else 0 End")
    suspend fun readStateChangeExists(itemId: Int): Boolean

    @Query("Select Case When :itemId In (Select id From ItemStateChange Where star_change = 1) Then 1 Else 0 End")
    suspend fun starStateChangeExists(itemId: Int): Boolean

    suspend fun upsertItemReadStateChange(item: Item, accountId: Int, useSeparateState: Boolean) {
        if (itemStateChangeExists(item.id, accountId)) {
            val oldItemReadState = if (useSeparateState)
                getItemReadState(item.remoteId!!, accountId)
            else
                getStandardItemReadState(item.remoteId!!, accountId)

            val readChange = item.isRead != oldItemReadState

            if (readChange) {
                val oldItemStateChange = selectItemStateChange(item.id)
                val newReadChange = !oldItemStateChange.readChange

                if (!newReadChange && !oldItemStateChange.starChange) {
                    delete(oldItemStateChange)
                } else {
                    updateItemReadStateChange(newReadChange, oldItemStateChange.id)
                }
            }
        } else {
            insert(ItemStateChange(id = item.id, readChange = true, accountId = accountId))
        }
    }

    suspend fun upsertItemStarStateChange(item: Item, accountId: Int, useSeparateState: Boolean) {
        if (itemStateChangeExists(item.id, accountId)) {
            val oldItemStarState = if (useSeparateState)
                getItemStarState(item.remoteId!!, accountId)
            else
                getStandardItemStarState(item.remoteId!!, accountId)

            val starChange = item.isStarred != oldItemStarState

            if (starChange) {
                val oldItemStateChange = selectItemStateChange(item.id)
                val newStarChange = !oldItemStateChange.starChange

                if (!newStarChange && !oldItemStateChange.readChange) {
                    delete(oldItemStateChange)
                } else {
                    updateItemStarStateChange(newStarChange, oldItemStateChange.id)
                }
            }
        } else {
            insert(ItemStateChange(id = item.id, starChange = true, accountId = accountId))
        }
    }

    @Query("Select * From ItemStateChange Where id = :id")
    fun selectItemStateChange(id: Int): ItemStateChange

    @Query("Select case When Exists (Select id, account_id From ItemStateChange Where id = :id And account_id = :accountId) Then 1 else 0 End")
    fun itemStateChangeExists(id: Int, accountId: Int): Boolean

    @Query("Select read From ItemState Where remote_id = :remoteId And account_id = :accountId")
    fun getItemReadState(remoteId: String, accountId: Int): Boolean

    @Query("Select read From Item Inner Join Feed On Item.feed_id = Feed.id Where Item.remoteId = :remoteId And account_id = :accountId")
    fun getStandardItemReadState(remoteId: String, accountId: Int): Boolean

    @Query("Select starred From ItemState Where remote_id = :remoteId And account_id = :accountId")
    fun getItemStarState(remoteId: String, accountId: Int): Boolean

    @Query("Select starred From Item Inner Join Feed On Item.feed_id = Feed.id Where Item.remoteId = :remoteId And account_id = :accountId")
    fun getStandardItemStarState(remoteId: String, accountId: Int): Boolean

    @Query("Update ItemStateChange set read_change = :readChange Where id = :id")
    fun updateItemReadStateChange(readChange: Boolean, id: Int)

    @Query("Update ItemStateChange set star_change = :starChange Where id = :id")
    fun updateItemStarStateChange(starChange: Boolean, id: Int)
}