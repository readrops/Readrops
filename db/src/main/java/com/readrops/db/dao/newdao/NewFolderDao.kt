package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.FolderWithFeed
import kotlinx.coroutines.flow.Flow

@Dao
interface NewFolderDao : NewBaseDao<Folder> {

    @Query("""
        Select Feed.id As feedId, Feed.name As feedName, Feed.icon_url As feedIcon, Feed.url As feedUrl, 
            Feed.siteUrl As feedSiteUrl, Feed.description as feedDescription, 
            Folder.id As folderId, Folder.name As folderName, Feed.account_id as accountId 
            From Feed Left Join Folder On Folder.id = Feed.folder_id
            Where Feed.folder_id is NULL OR Feed.folder_id is NOT NULL And Feed.id is NULL Or Feed.id is NOT NULL And Feed.account_id = :accountId Group By Feed.id 
        UNION ALL
        Select Feed.id As feedId, Feed.name As feedName, Feed.icon_url As feedIcon, Feed.url As feedUrl, 
            Feed.siteUrl As feedSiteUrl, Feed.description as feedDescription, 
            Folder.id As folderId, Folder.name As folderName, Folder.account_id as accountId 
            From Folder Left Join Feed On Folder.id = Feed.folder_id
            Where  Feed.id is NULL And Folder.account_id = :accountId
    """)
    fun selectFoldersAndFeeds(accountId: Int): Flow<List<FolderWithFeed>>

    @Query("Select * From Folder Where account_id = :accountId")
    fun selectFolders(accountId: Int): Flow<List<Folder>>

    @Query("Select * From Folder Where name = :name And account_id = :accountId")
    suspend fun selectFolderByName(name: String, accountId: Int): Folder?

    @Query("Select remoteId From Folder Where account_id = :accountId")
    suspend fun selectFolderRemoteIds(accountId: Int): List<String>

    @Query("Update Folder set name = :name Where remoteId = :remoteId And account_id = :accountId")
    suspend fun updateFolderName(name: String, remoteId: String, accountId: Int)

    @Query("Delete From Folder Where remoteId in (:ids) And account_id = :accountId")
    suspend fun deleteByIds(ids: List<String>, accountId: Int)

    /**
     * Insert, update and delete folders
     *
     * @param folders folders to insert or update
     * @param account owner of the feeds
     * @return the list of the inserted folders ids
     */
    @Transaction
    suspend fun upsertFolders(folders: List<Folder>, account: Account): List<Long> {
        val localFolderIds = selectFolderRemoteIds(account.id)

        val foldersToInsert = folders.filter { folder -> localFolderIds.none { localFolderId -> folder.remoteId == localFolderId  } }
        val foldersToDelete = localFolderIds.filter { localFolderId -> folders.none { folder -> localFolderId == folder.remoteId } }

        // folders to update
        folders.filter { folder -> localFolderIds.any { localFolderId -> folder.remoteId == localFolderId} }
            .forEach { updateFolderName(it.name!!, it.remoteId!!, account.id) }

        if (foldersToDelete.isNotEmpty()) {
            deleteByIds(foldersToDelete, account.id)
        }

        return insert(foldersToInsert)
    }
}