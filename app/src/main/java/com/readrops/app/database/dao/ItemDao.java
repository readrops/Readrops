package com.readrops.app.database.dao;


import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.PageKeyedDataSource;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.database.entities.Item;

import java.util.List;

@Dao
public interface ItemDao {

    @RawQuery(observedEntities = {Item.class, Folder.class, Feed.class})
    PageKeyedDataSource.Factory<Integer, ItemWithFeed> selectAll(SupportSQLiteQuery query);

    @Query("Select case When :guid In (Select guid from Item) Then 'true' else 'false' end")
    String guidExist(String guid);

    @Insert
    long insert(Item item);

    /**
     * Set an item read or unread
     * @param itemId id of the item to update
     * @param readState 1 for read, 0 for unread
     */
    @Query("Update Item set read = :readState Where id = :itemId")
    void setReadState(int itemId, int readState);

    @Query("Update Item set read = :readState")
    void setAllItemsReadState(int readState);

    @Query("Update Item set read = :readState Where feed_id = :feedId")
    void setAllItemsReadState(int feedId, int readState);

    @Query("Update Item set read_it_later = 1 Where id = :itemId")
    void setReadItLater(int itemId);

    @Query("Select count(*) From Item Where feed_id = :feedId And read = 0")
    int getUnreadCount(int feedId);

    @Query("Select title, Item.description, content, link, pub_date, image_link, author, read, text_color, background_color, read_time, Feed.name, Feed.id as feedId, siteUrl, Folder.id as folder_id, Folder.name as folder_name from Item Inner Join Feed, Folder on Item.feed_id = Feed.id And Item.id = :id And Folder.id = Feed.folder_id")
    LiveData<ItemWithFeed> getItemById(int id);
}
