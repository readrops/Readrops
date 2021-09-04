package com.readrops.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.FolderWithFeedCount
import java.util.ArrayList

@Dao
abstract class FolderDao : BaseDao<Folder> {

    @Query("Select * from Folder Where account_id = :accountId Order By name ASC")
    abstract fun getAllFolders(accountId: Int): LiveData<List<Folder>>

    @Query("Select Folder.*, count(Feed.id) as feed_count from Folder Left Join Feed on Folder.id = Feed.folder_id Where Folder.account_id = :accountId Group by Folder.id Order By name ASC")
    abstract fun getFoldersWithFeedCount(accountId: Int): LiveData<List<FolderWithFeedCount>>

    @Query("Select * from Folder Where account_id = :accountId Order By name ASC")
    abstract fun getFolders(accountId: Int): List<Folder>

    @Query("Update Folder set name = :name Where remoteId = :remoteFolderId And account_id = :accountId")
    abstract fun updateName(remoteFolderId: String, accountId: Int, name: String)

    @Query("Select case When :remoteId In (Select remoteId From Folder Where account_id = :accountId) Then 1 else 0 end")
    abstract fun remoteFolderExists(remoteId: String, accountId: Int): Boolean

    @Query("Select * from Folder Where id = :folderId")
    abstract fun select(folderId: Int): Folder

    @Query("Select remoteId From Folder Where account_id = :accountId")
    abstract fun getFolderRemoteIdsOfAccount(accountId: Int): MutableList<String>

    @Query("Delete From Folder Where remoteId in (:ids) And account_id = :accountId")
    abstract fun deleteByIds(ids: List<String>, accountId: Int)

    @Query("Select * From Folder Where name = :name And account_id = :accountId")
    abstract fun getFolderByName(name: String, accountId: Int): Folder

    /**
     * Insert, update and delete folders
     *
     * @param folders folders to insert or update
     * @param account owner of the feeds
     * @return the list of the inserted folders ids
     */
    @Transaction
    open fun foldersUpsert(folders: List<Folder>, account: Account): List<Long> {
        val accountFolderIds = getFolderRemoteIdsOfAccount(account.id)
        val foldersToInsert = arrayListOf<Folder>()

        for (folder in folders) {
            if (remoteFolderExists(folder.remoteId!!, account.id)) {
                updateName(folder.remoteId!!, account.id, folder.name!!)
                accountFolderIds.remove(folder.remoteId)
            } else {
                foldersToInsert.add(folder)
            }
        }

        if (accountFolderIds.isNotEmpty())
            deleteByIds(accountFolderIds, account.id)

        return insert(foldersToInsert)
    }
}