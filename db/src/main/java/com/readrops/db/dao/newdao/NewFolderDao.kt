package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Folder
import com.readrops.db.pojo.FolderWithFeed
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NewFolderDao : NewBaseDao<Folder> {

    @Query("Select Folder.id As folderId, Folder.name as folderName, Feed.id As feedId, Feed.name AS feedName, " +
            "Feed.icon_url As feedIcon, Feed.url as feedUrl, Feed.siteUrl as feedSiteUrl, count(*) As unreadCount, Folder.account_id as accountId " +
            "From Folder Left Join Feed On Folder.id = Feed.folder_id Left Join Item On Item.feed_id = Feed.id " +
            "Where Feed.folder_id is NULL OR Feed.folder_id is NOT NULL And Item.read = 0 " +
            "And Feed.account_id = :accountId Group By Feed.id, Folder.id Order By Folder.id")
    abstract fun selectFoldersAndFeeds(accountId: Int): Flow<List<FolderWithFeed>>

    @Query("Select * From Folder Where account_id = :accountId")
    abstract fun selectFolders(accountId: Int): Flow<List<Folder>>
}