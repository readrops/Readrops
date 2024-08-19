package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemStateChange
import com.readrops.db.pojo.ItemReadStarState

@Dao
interface ItemStateChangeDao: BaseDao<ItemStateChange> {

    @Query("Delete From ItemStateChange Where account_id = :accountId")
    suspend fun resetStateChanges(accountId: Int)

    @Query("""Select case When ItemState.remote_id is NULL Or ItemState.read = 1 Then 1 else 0 End read, 
        case When ItemState.remote_id is NULL Or ItemState.starred = 1 Then 1 else 0 End starred, 
        ItemStateChange.id as id, ItemStateChange.read_change, ItemStateChange.star_change, Item.remote_id From ItemStateChange 
        Inner Join Item On ItemStateChange.id = Item.id Left Join ItemState On ItemState.remote_id = Item.remote_id
        Where ItemStateChange.account_id = :accountId""")
    suspend fun selectItemStateChanges(accountId: Int): List<ItemReadStarState>

    @Query("Select Case When :itemId In (Select id From ItemStateChange Where read_change = 1) Then 1 Else 0 End")
    suspend fun readStateChangeExists(itemId: Int): Boolean

    @Query("Select Case When :itemId In (Select id From ItemStateChange Where star_change = 1) Then 1 Else 0 End")
    suspend fun starStateChangeExists(itemId: Int): Boolean

    suspend fun upsertItemReadStateChange(item: Item, accountId: Int, useSeparateState: Boolean) {
        if (itemStateChangeExists(item.id, accountId)) {
            val oldItemReadState = if (useSeparateState)
                selectItemReadState(item.remoteId!!, accountId)
            else
                selectStandardItemReadState(item.remoteId!!, accountId)

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
                selectItemStarState(item.remoteId!!, accountId)
            else
                selectStandardItemStarState(item.remoteId!!, accountId)

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
    suspend fun selectItemStateChange(id: Int): ItemStateChange

    @Query("Select case When Exists (Select id, account_id From ItemStateChange Where id = :id And account_id = :accountId) Then 1 else 0 End")
    suspend fun itemStateChangeExists(id: Int, accountId: Int): Boolean

    @Query("Select read From ItemState Where remote_id = :remoteId And account_id = :accountId")
    suspend fun selectItemReadState(remoteId: String, accountId: Int): Boolean

    @Query("Select read From Item Inner Join Feed On Item.feed_id = Feed.id Where Item.remote_id = :remoteId And account_id = :accountId")
    suspend fun selectStandardItemReadState(remoteId: String, accountId: Int): Boolean

    @Query("Select starred From ItemState Where remote_id = :remoteId And account_id = :accountId")
    suspend fun selectItemStarState(remoteId: String, accountId: Int): Boolean

    @Query("Select starred From Item Inner Join Feed On Item.feed_id = Feed.id Where Item.remote_id = :remoteId And account_id = :accountId")
    suspend fun selectStandardItemStarState(remoteId: String, accountId: Int): Boolean

    @Query("Update ItemStateChange set read_change = :readChange Where id = :id")
    suspend fun updateItemReadStateChange(readChange: Boolean, id: Int)

    @Query("Update ItemStateChange set star_change = :starChange Where id = :id")
    suspend fun updateItemStarStateChange(starChange: Boolean, id: Int)

    suspend fun upsertAllItemsReadStateChanges(accountId: Int) {
        upsertAllItemsReadStateChangesByUpdate(accountId)
        upsertAllItemsReadStateChangesByInsert(accountId)
    }

    @Query("""Update ItemStateChange Set read_change = 1 Where id In (Select Item.id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId)""")
    suspend fun upsertAllItemsReadStateChangesByUpdate(accountId: Int)

    @Query("""Insert Or Ignore Into ItemStateChange(id, read_change, star_change, account_id) 
        Select Item.id, 1 as read_change, 0 as star_change, account_id
        From Item Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId""")
    suspend fun upsertAllItemsReadStateChangesByInsert(accountId: Int)

    suspend fun upsertItemReadStateChangesByFeed(feedId: Int, accountId: Int) {
        upsertItemReadStateChangesByFeedByUpdate(feedId, accountId)
        upsertItemReadStateChangesByFeedByInsert(feedId, accountId)
    }

    @Query("""Update ItemStateChange Set read_change = 1 Where id In (Select Item.id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId And feed_id = :feedId)""")
    suspend fun upsertItemReadStateChangesByFeedByUpdate(feedId: Int, accountId: Int)

    @Query("""Insert Or Ignore Into ItemStateChange(id, read_change, star_change, account_id) 
        Select Item.id, 1 as read_change, 0 as star_change, account_id
        From Item Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId And feed_id = :feedId""")
    suspend fun upsertItemReadStateChangesByFeedByInsert(feedId: Int, accountId: Int)

    suspend fun upsertItemReadStateChangesByFolder(folderId: Int, accountId: Int) {
        upsertItemReadStateChangesByFolderByUpdate(folderId, accountId)
        upsertItemReadStateChangesByFolderByInsert(folderId, accountId)
    }

    @Query("""Update ItemStateChange Set read_change = 1 Where id In (Select Item.id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId And folder_id = :folderId)""")
    suspend fun upsertItemReadStateChangesByFolderByUpdate(folderId: Int, accountId: Int)

    @Query("""Insert Or Ignore Into ItemStateChange(id, read_change, star_change, account_id) 
        Select Item.id, 1 as read_change, 0 as star_change, account_id
        From Item Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId And folder_id = :folderId""")
    suspend fun upsertItemReadStateChangesByFolderByInsert(folderId: Int, accountId: Int)

    suspend fun upsertStarredItemReadStateChanges(accountId: Int) {
        upsertStarredItemReadStateChangesByUpdate(accountId)
        upsertStarredItemReadStateChangesByInsert(accountId)
    }

    @Query("""Update ItemStateChange Set read_change = 1 Where id In (Select Item.id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId And starred = 1)""")
    suspend fun upsertStarredItemReadStateChangesByUpdate(accountId: Int)

    @Query("""Insert Or Ignore Into ItemStateChange(id, read_change, star_change, account_id) 
        Select Item.id, 1 as read_change, 0 as star_change, account_id
        From Item Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId And starred = 1""")
    suspend fun upsertStarredItemReadStateChangesByInsert(accountId: Int)

    suspend fun upsertNewItemReadStateChanges(accountId: Int) {
        upsertNewItemReadStateChangesByUpdate(accountId)
        upsertNewItemReadStateChangesByInsert(accountId)
    }

    @Query("""Update ItemStateChange Set read_change = 1 Where id In (Select Item.id From Item 
        Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId
        And DateTime(Round(Item.pub_date / 1000), 'unixepoch') 
        Between DateTime(DateTime("now"), "-24 hour") And DateTime("now"))""")
    suspend fun upsertNewItemReadStateChangesByUpdate(accountId: Int)

    @Query("""Insert Or Ignore Into ItemStateChange(id, read_change, star_change, account_id) 
        Select Item.id, 1 as read_change, 0 as star_change, account_id
        From Item Inner Join Feed On Feed.id = Item.feed_id Where account_id = :accountId 
        And DateTime(Round(Item.pub_date / 1000), 'unixepoch') 
        Between DateTime(DateTime("now"), "-24 hour") And DateTime("now")""")
    suspend fun upsertNewItemReadStateChangesByInsert(accountId: Int)
}