package com.readrops.app.database.dao;


import androidx.lifecycle.LiveData;
import androidx.paging.PageKeyedDataSource;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;
import com.readrops.app.database.pojo.ItemWithFeed;

import java.util.List;

@Dao
public abstract class ItemDao implements BaseDao<Item> {

    @RawQuery(observedEntities = {Item.class, Folder.class, Feed.class})
    public abstract PageKeyedDataSource.Factory<Integer, ItemWithFeed> selectAll(SupportSQLiteQuery query);

    @Query("Select case When :guid In (Select guid From Item Inner Join Feed on Item.feed_id = Feed.id and account_id = :accountId) Then 1 else 0 end")
    public abstract boolean itemExists(String guid, int accountId);

    @Query("Select case When :remoteId In (Select remoteId from Item) And :feedId In (Select feed_id From Item) Then 1 else 0 end")
    public abstract boolean remoteItemExists(String remoteId, int feedId);

    /**
     * Set an item read or unread
     *
     * @param itemId      id of the item to update
     * @param readState   1 for read, 0 for unread
     * @param readChanged
     */
    @Query("Update Item Set read_changed = :readChanged, read = :readState Where id = :itemId")
    public abstract void setReadState(int itemId, int readState, int readChanged);

    @Query("Update Item set read_changed = 1, read = :readState Where feed_id In (Select id From Feed Where account_id = :accountId)")
    public abstract void setAllItemsReadState(int readState, int accountId);

    @Query("Update Item set read_changed = 1, read = :readState Where feed_id = :feedId")
    public abstract void setAllFeedItemsReadState(int feedId, int readState);

    @Query("Update Item set read_it_later = 1 Where id = :itemId")
    public abstract void setReadItLater(int itemId);

    @Query("Select count(*) From Item Where feed_id = :feedId And read = 0")
    public abstract int getUnreadCount(int feedId);

    @Query("Select title, Item.description, content, link, pub_date, image_link, author, read, text_color, " +
            "background_color, read_time, Feed.name, Feed.id as feedId, siteUrl, Folder.id as folder_id, " +
            "Folder.name as folder_name from Item Inner Join Feed On Item.feed_id = Feed.id Left Join Folder on Folder.id = Feed.folder_id Where Item.id = :id")
    public abstract LiveData<ItemWithFeed> getItemById(int id);

    @Query("Select remoteId From Item Where read_changed = 1 And read = 1")
    public abstract List<String> getReadChanges();

    @Query("Select remoteId From Item Where read_changed = 1 And read = 0")
    public abstract List<String> getUnreadChanges();

    @Query("Update Item set read_changed = 0")
    public abstract void resetReadChanges();
}
