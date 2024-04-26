package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Folder
import com.readrops.db.pojo.FolderWithFeed
import kotlinx.coroutines.flow.Flow

@Dao
interface NewFolderDao : NewBaseDao<Folder> {

    @Query("Select Feed.id As feedId, Feed.name As feedName, Feed.icon_url As feedIcon, Feed.url As feedUrl, " +
            "Feed.siteUrl As feedSiteUrl, Feed.description as feedDescription, Feed.account_id As accountId, " +
            "Folder.id As folderId, Folder.name As folderName, 0 as unreadCount " +
            " From Feed Left Join Folder On Folder.id = Feed.folder_id " +
            "Where Feed.folder_id is NULL OR Feed.folder_id is NOT NULL And Feed.account_id = :accountId Group By Feed.id")
    fun selectFoldersAndFeeds(accountId: Int): Flow<List<FolderWithFeed>>

    @Query("Select * From Folder Where account_id = :accountId")
    fun selectFolders(accountId: Int): Flow<List<Folder>>

    @Query("Select * From Folder Where name = :name And account_id = :accountId")
    suspend fun selectFolderByName(name: String, accountId: Int): Folder?
}