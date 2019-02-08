package com.readrops.app.database.dao;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.readrops.app.database.ItemWithFeed;
import com.readrops.app.database.entities.Item;

import java.util.List;

@Dao
public interface ItemDao {

    @Query("Select * from Item Where feed_id = :feedId")
    LiveData<List<Item>> getAllByFeed(int feedId);

    @Query("Select * from Item Order By pub_date DESC")
    LiveData<List<Item>> getAll();

    @Query("Select Item.id, title, clean_description, image_link, pub_date, name, color, icon_url, read_time from Item Inner Join Feed on Item.feed_id = Feed.id Order By Item.id DESC")
    LiveData<List<ItemWithFeed>> getAllItemWithFeeds();

    @Query("Select case When :guid In (Select guid from Item) Then 'true' else 'false' end")
    String guidExist(String guid);

    @Insert
    long insert(Item item);

    @Insert
    void insertAll(List<Item> items);

    @Query("Select title, Item.description, content, pub_date, author, 0 as color, read_time, name from Item Inner Join Feed on Item.feed_id = Feed.id And Item.id = :id")
    LiveData<ItemWithFeed> getItemById(int id);
}
