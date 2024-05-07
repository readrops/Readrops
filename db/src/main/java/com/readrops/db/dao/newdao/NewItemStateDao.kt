package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.ItemState

@Dao
interface NewItemStateDao : NewBaseDao<ItemState> {

    @Query("Delete From ItemState Where account_id = :accountId")
    suspend fun deleteItemStates(accountId: Int)

    @Query("Update ItemState set read = :read Where remote_id = :remoteId And account_id = :accountId")
    suspend fun updateItemReadState(read: Boolean, remoteId: String, accountId: Int)

    @Query("Update ItemState set starred = :star Where remote_id = :remoteId And account_id = :accountId")
    suspend fun updateItemStarState(star: Boolean, remoteId: String, accountId: Int)

    @Query("Select case When Exists (Select remote_id, account_id From ItemState Where remote_id = :remoteId And account_id = :accountId) Then 1 else 0 End")
    suspend fun itemStateExists(remoteId: String, accountId: Int): Boolean

    suspend fun upsertItemReadState(itemState: ItemState) {
        if (itemStateExists(itemState.remoteId, itemState.accountId)) {
            updateItemReadState(itemState.read, itemState.remoteId, itemState.accountId)
        } else {
            insert(itemState)
        }
    }

    suspend fun upsertItemStarState(itemState: ItemState) {
        if (itemStateExists(itemState.remoteId, itemState.accountId)) {
            updateItemStarState(itemState.starred, itemState.remoteId, itemState.accountId)
        } else {
            insert(itemState)
        }
    }
}