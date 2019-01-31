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

    @Query("Select Item.id, title, Item.description, image_link, pub_date, name from Item Inner Join Feed on Item.feed_id = Feed.id Order By pub_date DESC, Item.id")
    LiveData<List<ItemWithFeed>> getAllItemWithFeeds();

    @Query("Select case When :guid In (Select guid from Item) Then 'true' else 'false' end")
    String guidExist(String guid);

    @Insert
    void insert(Item item);

    @Insert
    void insertAll(List<Item> items);
}
