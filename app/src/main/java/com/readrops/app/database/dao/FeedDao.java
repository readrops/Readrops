package com.readrops.app.database.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.pojo.FeedWithFolder;

import java.util.List;

@Dao
public interface FeedDao {

    @Query("Select * from Feed order by name ASC")
    List<Feed> getAllFeeds();

    @Insert
    long insert(Feed feed);

    @Update
    void update(Feed feed);

    @Query("Delete From Feed Where id = :feedId")
    void delete(int feedId);

    @Query("Select case When :feedUrl In (Select url from Feed) Then 'true' else 'false' end")
    String feedExists(String feedUrl);

    @Query("Select count(*) from Feed")
    int getFeedCount();

    @Query("Select * from Feed Where url = :feedUrl")
    Feed getFeedByUrl(String feedUrl);

    @Query("Select * from Feed Where folder_id = :folderId")
    List<Feed> getFeedsByFolder(int folderId);

    @Query("Update Feed set etag = :etag, last_modified = :lastModified Where id = :feedId")
    void updateHeaders(String etag, String lastModified, int feedId);

    @Query("Update Feed set folder_id = :folderId Where id = :feedId")
    void updateFeedFolder(int feedId, int folderId);

    @Query("Update Feed set name = :feedName, url = :feedUrl, folder_id = :folderId Where id = :feedId")
    void updateFeedFields(int feedId, String feedName, String feedUrl, int folderId);

    @Query("Select Feed.name as feed_name, Feed.id as feed_id, Folder.name as folder_name, Folder.id as folder_id," +
            "Feed.description as feed_description, Feed.icon_url as feed_icon_url, Feed.url as feed_url, Feed.folder_id as feed_folder_id" +
            ", Feed.siteUrl as feed_siteUrl from Feed Inner Join Folder on Feed.folder_id = Folder.id Order by Feed.name")
    LiveData<List<FeedWithFolder>> getAllFeedsWithFolder();
}

