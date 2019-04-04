package com.readrops.app.database.dao;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.database.entities.Item;

import java.util.List;

@Dao
public interface ItemDao {

    @Query("Select * from Item Where feed_id = :feedId")
    LiveData<List<Item>> getAllByFeed(int feedId);

    @Query("Select * from Item Order By pub_date DESC")
    LiveData<List<Item>> getAll();

    @Query("Select Item.id, title, clean_description, image_link, pub_date, read, Feed.name, text_color, background_color, icon_url, read_time, Feed.id as feedId, Folder.id as folder_id, Folder.name as folder_name from Item Inner Join Feed, Folder on Item.feed_id = Feed.id And Folder.id = Feed.folder_id Order By Item.id DESC")
    LiveData<List<ItemWithFeed>> getAllItemWithFeeds();

    @Query("Select case When :guid In (Select guid from Item) Then 'true' else 'false' end")
    String guidExist(String guid);

    @Insert
    long insert(Item item);

    @Insert
    void insertAll(List<Item> items);

    @Query("Update Item set read = 1 Where id = :itemId")
    void setRead(int itemId);

    @Query("Select count(*) From Item Where feed_id = :feedId And read = 0")
    int getUnreadCount(int feedId);

    @Query("Select title, Item.description, content, link, pub_date, image_link, author, read, text_color, background_color, read_time, Feed.name, Feed.id as feedId, siteUrl, Folder.id as folder_id, Folder.name as folder_name from Item Inner Join Feed, Folder on Item.feed_id = Feed.id And Item.id = :id And Folder.id = Feed.folder_id")
    LiveData<ItemWithFeed> getItemById(int id);
}
