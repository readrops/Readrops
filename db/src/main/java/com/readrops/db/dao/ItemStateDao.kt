package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.readrops.db.entities.ItemState

@Dao
interface ItemStateDao : BaseDao<ItemState> {

    @Query("Select * From ItemState Where account_id = :accountId And remote_id = :remoteId")
    suspend fun selectItemState(accountId: Int, remoteId: String): ItemState

    @Query("Delete From ItemState Where account_id = :accountId")
    suspend fun deleteItemStates(accountId: Int)

    @Query("Update ItemState set read = :read Where remote_id = :remoteId And account_id = :accountId")
    suspend fun updateItemReadState(read: Boolean, remoteId: String, accountId: Int)

    @Query("Update ItemState set starred = :star Where remote_id = :remoteId And account_id = :accountId")
    suspend fun updateItemStarState(star: Boolean, remoteId: String, accountId: Int)

    @Query("""Select case When Exists (Select remote_id, account_id From ItemState 
        Where remote_id = :remoteId And account_id = :accountId) Then 1 else 0 End""")
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

    @Query("""Update ItemState set read = 1 Where remote_id In (Select Item.remote_id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId And starred = 1)""")
    suspend fun setAllStarredItemsRead(accountId: Int)

    //region items read by ids

    @Transaction
    suspend fun setItemsRead(ids: List<String>, itemStates: List<ItemState>, accountId: Int) {
        setItemsReadByUpdate(ids, accountId)
        insertIgnoreConflicts(itemStates)
    }

    @Query("""Update ItemState set read = 1 Where remote_id In (:ids) And account_id = :accountId""")
    suspend fun setItemsReadByUpdate(ids: List<String>, accountId: Int)

    //endregion

    //region all items read

    @Transaction
    suspend fun setAllItemsRead(accountId: Int) {
        setAllItemsReadByUpdate(accountId)
        setAllItemsReadByInsert(accountId)
    }

    @Query("""Update ItemState set read = 1 Where remote_id In (Select Item.remote_id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId)""")
    suspend fun setAllItemsReadByUpdate(accountId: Int)

    @Query("""Insert Or Ignore Into ItemState(read, starred, remote_id, account_id) Select 1 as read, 0 as starred, 
        Item.remote_id as remote_id, account_id From Item Inner Join Feed On Feed.id = Item.feed_id Where Feed.account_id = :accountId""")
    suspend fun setAllItemsReadByInsert(accountId: Int)

    //endregion

    //region read by feed

    @Transaction
    suspend fun setAllItemsReadByFeed(feedId: Int, accountId: Int) {
        setAllItemsReadByFeedByUpdate(feedId, accountId)
        setAllItemsReadByFeedByInsert(feedId, accountId)
    }

    @Query("""Update ItemState set read = 1 Where remote_id In (Select Item.remote_id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId and feed_id = :feedId)""")
    suspend fun setAllItemsReadByFeedByUpdate(feedId: Int, accountId: Int)

    @Query("""Insert Or Ignore Into ItemState(read, starred, remote_id, account_id) Select 1 as read, 0 as starred, 
        Item.remote_id As remote_id, account_id From Item Inner Join Feed Where Feed.account_id = :accountId 
        And feed_id = :feedId Group By Item.remote_id""")
    suspend fun setAllItemsReadByFeedByInsert(feedId: Int, accountId: Int)

    //endregion

    //region read by folder

    @Transaction
    suspend fun setAllItemsReadByFolder(folderId: Int, accountId: Int) {
        setAllItemsReadByFolderByUpdate(folderId, accountId)
        setAllItemsReadByFolderByInsert(folderId, accountId)
    }

    @Query("""Update ItemState set read = 1 Where remote_id In (Select Item.remote_id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId and folder_id = :folderId)""")
    suspend fun setAllItemsReadByFolderByUpdate(folderId: Int, accountId: Int)

    @Query("""Insert Or Ignore Into ItemState(read, starred, remote_id, account_id) Select 1 as read, 0 as starred, 
        Item.remote_id As remote_id, account_id From Item Inner Join Feed On Feed.id = Item.feed_id
        Where Feed.account_id = :accountId And folder_id = :folderId Group By Item.remote_id""")
    suspend fun setAllItemsReadByFolderByInsert(folderId: Int, accountId: Int)

    //endregion

    //region news items read

    @Transaction
    suspend fun setAllNewItemsRead(accountId: Int) {
        setAllNewItemsReadByUpdate(accountId)
        setAllNewItemsReadByInsert(accountId)
    }

    @Query("""Update ItemState set read = 1 Where remote_id In (Select Item.remote_id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId 
        And DateTime(Round(Item.pub_date / 1000), 'unixepoch') 
        Between DateTime(DateTime("now"), "-24 hour") And DateTime("now"))""")
    suspend fun setAllNewItemsReadByUpdate(accountId: Int)

    @Query("""Insert Or Ignore Into ItemState(read, starred, remote_id, account_id) Select 1 as read, 0 as starred, 
        Item.remote_id As remote_id, account_id From Item Inner Join Feed On Feed.id = Item.feed_id 
        Where Feed.account_id = :accountId And DateTime(Round(Item.pub_date / 1000), 'unixepoch') 
        Between DateTime(DateTime("now"), "-24 hour") And DateTime("now")""")
    suspend fun setAllNewItemsReadByInsert(accountId: Int)

    //endregion
}