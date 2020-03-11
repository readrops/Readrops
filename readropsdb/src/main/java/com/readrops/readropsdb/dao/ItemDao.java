package com.readrops.readropsdb.dao;


import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.RoomWarnings;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropsdb.entities.Item;
import com.readrops.readropsdb.pojo.ItemWithFeed;

import java.util.List;

import io.reactivex.Completable;

@Dao
public interface ItemDao extends BaseDao<Item> {

    @RawQuery(observedEntities = {Item.class, Folder.class, Feed.class})
    DataSource.Factory<Integer, ItemWithFeed> selectAll(SupportSQLiteQuery query);

    @Query("Select * From Item Where id = :itemId")
    Item select(int itemId);

    @Query("Select case When :guid In (Select guid From Item Inner Join Feed on Item.feed_id = Feed.id and account_id = :accountId) Then 1 else 0 end")
    boolean itemExists(String guid, int accountId);

    @Query("Select case When :remoteId In (Select remoteId from Item) And :feedId In (Select feed_id From Item) Then 1 else 0 end")
    boolean remoteItemExists(String remoteId, int feedId);

    @Query("Select * From Item Where remoteId = :remoteId And feed_id = :feedId")
    Item selectByRemoteId(String remoteId, int feedId);

    /**
     * Set an item read or unread
     *
     * @param itemId      id of the item to update
     * @param read   1 for read, 0 for unread
     * @param readChanged
     */
    @Query("Update Item Set read_changed = :readChanged, read = :read Where id = :itemId")
    Completable setReadState(int itemId, boolean read, boolean readChanged);

    @Query("Update Item set read_changed = 1, read = :readState Where feed_id In (Select id From Feed Where account_id = :accountId)")
    Completable setAllItemsReadState(int readState, int accountId);

    @Query("Update Item set read_changed = 1, read = :readState Where feed_id = :feedId")
    Completable setAllFeedItemsReadState(int feedId, int readState);

    @Query("Update Item set read_it_later = 1 Where id = :itemId")
    Completable setReadItLater(int itemId);

    @Query("Select count(*) From Item Where feed_id = :feedId And read = 0")
    int getUnreadCount(int feedId);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("Select title, Item.description, content, link, pub_date, image_link, author, read, text_color, " +
            "background_color, read_time, Feed.name, Feed.id as feedId, siteUrl, Folder.id as folder_id, " +
            "Folder.name as folder_name from Item Inner Join Feed On Item.feed_id = Feed.id Left Join Folder on Folder.id = Feed.folder_id Where Item.id = :id")
    LiveData<ItemWithFeed> getItemById(int id);

    @Query("Select Item.remoteId From Item Inner Join Feed On Item.feed_id = Feed.id Where read_changed = 1 And read = 1 And account_id = :accountId")
    List<String> getReadChanges(int accountId);

    @Query("Select Item.remoteId From Item Inner Join Feed On Item.feed_id = Feed.id Where read_changed = 1 And read = 0 And account_id = :accountId")
    List<String> getUnreadChanges(int accountId);

    @Query("Update Item set read_changed = 0 Where feed_id in (Select id From Feed Where account_id = :accountId)")
    void resetReadChanges(int accountId);

    @Query("Update Item set read = :read Where remoteId = :remoteId")
    void setReadState(String remoteId, boolean read);
}
