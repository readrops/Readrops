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

    @Query("Insert Or Replace Into ItemState(read, remote_id) Select 1 as read, Item.remoteId From Item Inner Join Feed On Feed.account_id = :accountId")
    suspend fun setAllItemsRead(accountId: Int)

    suspend fun setAllItemsReadByFeed(feedId: Int, accountId: Int) {
        setAllItemsReadByFeedByUpdate(feedId, accountId)
        //setAllItemsReadByFeedByInsert(feedId, accountId) //TODO use this after putting ItemState.remoteId UNIQUE?
    }

    @Query("""Update ItemState set read = 1 Where remote_id In (Select Item.remoteId From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId and feed_id = :feedId)""")
    suspend fun setAllItemsReadByFeedByUpdate(feedId: Int, accountId: Int)

    @Query("""Insert Or Ignore Into ItemState(read, starred, remote_id) Select 1 as read, 0 as starred, 
        Item.remoteId From Item Inner Join Feed Where Feed.account_id = :accountId And feed_id = :feedId""")
    suspend fun setAllItemsReadByFeedByInsert(feedId: Int, accountId: Int)
}