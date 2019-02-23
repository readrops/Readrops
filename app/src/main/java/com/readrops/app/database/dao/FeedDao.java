package com.readrops.app.database.dao;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.pojo.FeedWithFolder;

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

    @Query("Update Feed set etag = :etag, last_modified = :lastModified Where id = :feedId")
    void updateHeaders(String etag, String lastModified, int feedId);

    @Query("Update Feed set folder_id = :folderId Where id = :feedId")
    void updateFeedFolder(int feedId, int folderId);

    @Query("Select Feed.name as feed_name, Feed.id as feed_id, Folder.name as folder_name, Folder.id as folder_id," +
            "Feed.description as feed_description, Feed.icon_url as feed_icon_url, Feed.url as feed_url" +
            " from Feed Inner Join Folder on Feed.folder_id = Folder.id Order by Feed.name")
    LiveData<List<FeedWithFolder>> getAllFeedsWithFolder();
}
