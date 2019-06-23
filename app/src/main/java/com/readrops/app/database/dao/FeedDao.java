package com.readrops.app.database.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.pojo.FeedWithFolder;

import java.util.List;

@Dao
public interface FeedDao {

    @Query("Select * from Feed Where account_id = :accountId order by name ASC")
    List<Feed> getAllFeeds(int accountId);

    @Insert
    long insert(Feed feed);

    @Insert
    long[] insert(List<Feed> feeds);

    @Query("Delete From Feed Where id = :feedId")
    void delete(int feedId);

    @Query("Select case When :feedUrl In (Select url from Feed Where account_id = :accountId) Then 1 else 0 end")
    boolean feedExists(String feedUrl, int accountId);

    @Query("Select case When :remoteId In (Select remoteId from Feed Where account_id = :accountId) Then 1 else 0 end")
    boolean remoteFeedExists(int remoteId, int accountId);

    @Query("Select count(*) from Feed Where account_id = :accountId")
    int getFeedCount(int accountId);

    @Query("Select * from Feed Where url = :feedUrl")
    Feed getFeedByUrl(String feedUrl);

    @Query("Select * from Feed Where remoteId = :remoteId And account_id = :accountId")
    Feed getFeedByRemoteId(int remoteId, int accountId);

    @Query("Select * from Feed Where folder_id = :folderId")
    List<Feed> getFeedsByFolder(int folderId);

    @Query("Select * from Feed Where account_id = :accountId And folder_id is null")
    List<Feed> getFeedsWithoutFolder(int accountId);

    @Query("Update Feed set etag = :etag, last_modified = :lastModified Where id = :feedId")
    void updateHeaders(String etag, String lastModified, int feedId);

    @Query("Update Feed set folder_id = :folderId Where id = :feedId")
    void updateFeedFolder(int feedId, Integer folderId);

    @Query("Update Feed set name = :feedName, url = :feedUrl, folder_id = :folderId Where id = :feedId")
    void updateFeedFields(int feedId, String feedName, String feedUrl, Integer folderId);

    @Query("Update Feed set text_color = :textColor, background_color = :bgColor Where id = :feedId")
    void updateColors(int feedId, int textColor, int bgColor);

    @Query("Select Feed.name as feed_name, Feed.id as feed_id, Folder.name as folder_name, Folder.id as folder_id," +
            "Feed.description as feed_description, Feed.icon_url as feed_icon_url, Feed.url as feed_url, Feed.folder_id as feed_folder_id" +
            ", Feed.siteUrl as feed_siteUrl from Feed Left Join Folder on Feed.folder_id = Folder.id Order by Feed.name")
    LiveData<List<FeedWithFolder>> getAllFeedsWithFolder();

    @Query("Select * From Feed Where id in (:ids)")
    List<Feed> selectFromIdList(long[] ids);
}

