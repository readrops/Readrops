package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.readrops.db.entities.ItemState
import io.reactivex.Completable

@Dao
interface ItemStateDao : BaseDao<ItemState> {

    @Query("Delete From ItemState Where account_id = :accountId")
    fun deleteItemsStates(accountId: Int)

    @Query("Delete From ItemState Where remote_id = :remoteId And account_id = :accountId")
    fun deleteItemState(remoteId: String, accountId: Int)

    @Insert
    fun insertItemStates(items: List<ItemState>)

    @Insert
    fun insertItemState(itemState: ItemState)

    @Query("Update ItemState set read = :read Where remote_id = :remoteId And account_id = :accountId")
    fun updateItemReadState(read: Boolean, remoteId: String, accountId: Int)

    @Query("Select case When Exists (Select remote_id, account_id From ItemState Where remote_id = :remoteId And account_id = :accountId) Then 1 else 0 End")
    fun itemStateExists(remoteId: String, accountId: Int): Boolean

    fun upsertItemReadState(itemState: ItemState) = Completable.create {
        if (itemStateExists(itemState.remoteId, itemState.accountId)) {
            updateItemReadState(itemState.read, itemState.remoteId, itemState.accountId)
        } else {
            insertItemState(itemState)
        }

        it.onComplete()
    }
}