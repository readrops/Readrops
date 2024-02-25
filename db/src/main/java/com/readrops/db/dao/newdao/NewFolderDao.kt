package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Folder
import com.readrops.db.pojo.FolderWithFeed
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NewFolderDao : NewBaseDao<Folder> {

    @Query("With main As (Select Folder.id As folderId, Folder.name as folderName, Feed.id As feedId, " +
            "Feed.name AS feedName, Feed.icon_url As feedIcon, Feed.url as feedUrl, Feed.siteUrl as feedSiteUrl, " +
            "Folder.account_id as accountId, Item.read as itemRead " +
            "From Folder Left Join Feed On Folder.id = Feed.folder_id Left Join Item On Item.feed_id = Feed.id " +
            "Where Feed.folder_id is NULL OR Feed.folder_id is NOT NULL And Feed.account_id = :accountId ) " +
            "Select folderId, folderName, feedId, feedName, feedIcon, feedUrl, feedSiteUrl, accountId," +
            " (Select count(*) From main Where (itemRead = 0)) as unreadCount " +
            "From main Group by feedId, folderId Order By folderName, feedName")
    abstract fun selectFoldersAndFeeds(accountId: Int): Flow<List<FolderWithFeed>>

    @Query("Select * From Folder Where account_id = :accountId")
    abstract fun selectFolders(accountId: Int): Flow<List<Folder>>
}