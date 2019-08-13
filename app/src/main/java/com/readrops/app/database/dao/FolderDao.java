package com.readrops.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Folder;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class FolderDao implements BaseDao<Folder> {

    @Query("Select * from Folder Where account_id = :accountId Order By name ASC")
    public abstract LiveData<List<Folder>> getAllFolders(int accountId);

    @Query("Select * from Folder Where account_id = :accountId Order By name ASC")
    public abstract List<Folder> getFolders(int accountId);

    @Query("Update Folder set name = :name Where remoteId = :remoteFolderId And account_id = :accountId")
    public abstract void updateName(String remoteFolderId, int accountId, String name);

    @Query("Select case When :remoteId In (Select remoteId From Folder Where account_id = :accountId) Then 1 else 0 end")
    abstract boolean remoteFolderExists(String remoteId, int accountId);

    @Query("Select * from Folder Where id = :folderId")
    public abstract Folder select(int folderId);

    @Query("Select remoteId From Folder Where account_id = :accountId")
    public abstract List<String> getFolderRemoteIdsOfAccount(int accountId);

    @Query("Delete From Folder Where id in (:ids)")
    abstract void deleteByIds(List<String> ids);

    /**
     * Insert, update and delete folders
     *
     * @param folders folders to insert or update
     * @param account owner of the feeds
     * @return the list of the inserted feeds ids
     */
    @Transaction
    public List<Long> foldersUpsert(List<Folder> folders, Account account) {
        List<String> accountFolderIds = getFolderRemoteIdsOfAccount(account.getId());
        List<Folder> foldersToInsert = new ArrayList<>();

        for (Folder folder : folders) {
            if (remoteFolderExists(folder.getRemoteId(), account.getId())) {
                updateName(folder.getRemoteId(), account.getId(), folder.getName());

                accountFolderIds.remove(folder.getRemoteId());
            } else {
                foldersToInsert.add(folder);
            }
        }

        if (!accountFolderIds.isEmpty())
            deleteByIds(accountFolderIds);

        return insert(foldersToInsert);
    }
}
