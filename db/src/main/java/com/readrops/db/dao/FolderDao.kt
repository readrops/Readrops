package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.FolderWithFeed
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao : BaseDao<Folder> {

    // TODO react to Item changes when this table is not part of the query might be a perf issue
    @RawQuery(observedEntities = [Folder::class, Feed::class, Item::class])
    fun selectFoldersAndFeeds(query: SupportSQLiteQuery): Flow<List<FolderWithFeed>>

    @Query("Select * From Folder Where account_id = :accountId")
    fun selectFolders(accountId: Int): Flow<List<Folder>>

    @Query("Select * from Folder Where id = :folderId")
    fun select(folderId: Int): Folder

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