package com.readrops.app.database.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.readrops.app.database.entities.Feed;

import java.util.List;

@Dao
public interface FeedDao {

    @Query("Select * from Feed order by name ASC")
    List<Feed> getAllFeeds();

    @Insert
    long insert(Feed feed);

    @Query("Select count(*) from Feed")
    int getFeedCount();

    @Query("Select * from Feed Where url = :feedUrl")
    Feed getFeedByUrl(String feedUrl);

    @Query("Select id from Feed Where url = :feedUrl")
    int getFeedIdByUrl(String feedUrl);

    @Query("Update Feed set etag = :etag, last_modified = :lastModified Where id = :feedId")
    void updateHeaders(String etag, String lastModified, int feedId);

}
