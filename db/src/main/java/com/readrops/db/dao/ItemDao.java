package com.readrops.db.dao;


import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.Item;
import com.readrops.db.entities.ItemState;
import com.readrops.db.pojo.ItemWithFeed;
import com.readrops.db.pojo.StarItem;

import java.util.List;

import io.reactivex.Completable;

@Dao
public interface ItemDao extends BaseDao<Item> {

    @RawQuery(observedEntities = {Item.class, Folder.class, Feed.class, ItemState.class})
    DataSource.Factory<Integer, ItemWithFeed> selectAll(SupportSQLiteQuery query);

    @Query("Select * From Item Where id = :itemId")
    Item select(int itemId);

    @Query("Select case When :guid In (Select guid From Item Inner Join Feed on Item.feed_id = Feed.id and account_id = :accountId) Then 1 else 0 end")
    boolean itemExists(String guid, int accountId);

    @Query("Select case When :remoteId In (Select remoteId from Item) And :feedId In (Select feed_id From Item) Then 1 else 0 end")
    boolean remoteItemExists(String remoteId, int feedId);

    @Query("Select * From Item Where remoteId = :remoteId And feed_id = :feedId")
    Item selectByRemoteId(String remoteId, int feedId);

    @Query("Update Item Set read = :read Where id = :itemId")
    Completable setReadState(int itemId, boolean read);

    @Query("Update Item set starred = :starred Where id = :itemId")
    Completable setStarState(int itemId, boolean starred);

    @Query("Update Item set read = :readState Where feed_id In (Select id From Feed Where account_id = :accountId)")
    Completable setAllItemsReadState(int readState, int accountId);

    @Query("Update Item set read = :readState Where feed_id = :feedId")
    Completable setAllFeedItemsReadState(int feedId, int readState);

    @Query("Update Item set read_it_later = 1 Where id = :itemId")
    Completable setReadItLater(int itemId);

    @Query("Select count(*) From Item Where feed_id = :feedId And read = 0")
    int getUnreadCount(int feedId);
    
    @RawQuery(observedEntities = {Item.class, ItemState.class})
    LiveData<ItemWithFeed> getItemById(SupportSQLiteQuery query);

    @Query("Select Item.guid, Feed.remoteId as feedRemoteId From Item Inner Join Feed On Item.feed_id = Feed.id Where Item.remoteId In (:remoteIds) And account_id = :accountId")
    List<StarItem> getStarChanges(List<String> remoteIds, int accountId);

    @Query("Update Item set read = :read, starred = :starred Where remoteId = :remoteId")
    void setReadAndStarState(String remoteId, boolean read, boolean starred);
}
