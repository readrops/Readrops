package com.readrops.app.database.dao;


import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.PageKeyedDataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.database.entities.Item;

import java.util.List;

@Dao
public interface ItemDao {

    String SELECT_ALL_FIELDS = "Item.id, title, clean_description, image_link, pub_date, read, read_it_later, " +
            "Feed.name, text_color, background_color, icon_url, read_time, Feed.id as feedId, Folder.id as folder_id, " +
            "Folder.name as folder_name";

    String SELECT_ALL_JOIN = "Item Inner Join Feed, Folder on Item.feed_id = Feed.id And Folder.id = Feed.folder_id";

    String SELECT_ALL_ORDER_BY_ASC = "Order by Item.id DESC";

    String SELECT_ALL_ORDER_BY_DESC = "Order By pub_date ASC";

    @Query("Select " + SELECT_ALL_FIELDS + " from " + SELECT_ALL_JOIN + " Where feed_id = :feedId " +
            "And read = :readState And read_it_later = 0 " + SELECT_ALL_ORDER_BY_ASC)
    DataSource.Factory<Integer, ItemWithFeed> selectAllByFeedASC(int feedId, int readState);

    @Query("Select " + SELECT_ALL_FIELDS + " from " + SELECT_ALL_JOIN + " Where feed_id = :feedId " +
            "And read = :readState And read_it_later = 0 " + SELECT_ALL_ORDER_BY_DESC)
    DataSource.Factory<Integer, ItemWithFeed> selectAllByFeedsDESC(int feedId, int readState);

    @Query("Select " + SELECT_ALL_FIELDS + " from " + SELECT_ALL_JOIN + " Where read_it_later = 1 " +
            "And read = :readState " + SELECT_ALL_ORDER_BY_ASC)
    DataSource.Factory<Integer, ItemWithFeed> selectAllReadItLaterASC(int readState);

    @Query("Select " + SELECT_ALL_FIELDS + " from " + SELECT_ALL_JOIN + " Where read_it_later = 1 " +
            "And read = :readState " + SELECT_ALL_ORDER_BY_DESC)
    DataSource.Factory<Integer, ItemWithFeed> selectAllReadItLaterDESC(int readState);

    /**
     * ASC means here from the newest (inserted) to the oldest
     */
    @Query("Select " + SELECT_ALL_FIELDS + " From " + SELECT_ALL_JOIN + " Where read = :readState And " +
            "read_it_later = 0 " + SELECT_ALL_ORDER_BY_ASC)
    DataSource.Factory<Integer, ItemWithFeed> selectAllASC(int readState);

    /**
     * DESC means here from the oldest to the newest
     */
    @Query("Select " + SELECT_ALL_FIELDS + " From " + SELECT_ALL_JOIN + " Where read = :readState And "
            + "read_it_later = 0 " + SELECT_ALL_ORDER_BY_DESC)
    PageKeyedDataSource.Factory<Integer, ItemWithFeed> selectAllDESC(int readState);

    @Query("Select case When :guid In (Select guid from Item) Then 'true' else 'false' end")
    String guidExist(String guid);

    @Insert
    long insert(Item item);

    @Insert
    void insertAll(List<Item> items);

    /**
     * Set an item read or unread
     * @param itemId if of the item to update
     * @param readState 1 for read, 0 for unread
     */
    @Query("Update Item set read = :readState Where id = :itemId")
    void setReadState(int itemId, int readState);

    @Query("Update Item set read_it_later = 1 Where id = :itemId")
    void setReadItLater(int itemId);

    @Query("Select count(*) From Item Where feed_id = :feedId And read = 0")
    int getUnreadCount(int feedId);

    @Query("Select title, Item.description, content, link, pub_date, image_link, author, read, text_color, background_color, read_time, Feed.name, Feed.id as feedId, siteUrl, Folder.id as folder_id, Folder.name as folder_name from Item Inner Join Feed, Folder on Item.feed_id = Feed.id And Item.id = :id And Folder.id = Feed.folder_id")
    LiveData<ItemWithFeed> getItemById(int id);
}
