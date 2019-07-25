package com.readrops.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.readrops.app.database.entities.Folder;

import java.util.List;

@Dao
public interface FolderDao {

    @Query("Select * from Folder Where account_id = :accountId Order By name ASC")
    LiveData<List<Folder>> getAllFolders(int accountId);

    @Query("Select * from Folder Where account_id = :accountId Order By name ASC")
    List<Folder> getFolders(int accountId);

    @Insert
    long insert(Folder folder);

    @Insert
    long[] insert(List<Folder> folders);

    @Update
    void update(Folder folder);

    @Query("Update Folder set name = :name Where remoteId = :remoteFolderId And account_id = :accountId")
    void updateName(int remoteFolderId, int accountId, String name);

    @Delete
    void delete(Folder folder);

    @Query("Select id From Folder Where remoteId = :remoteId And account_id = :accountId")
    int getRemoteFolderLocalId(int remoteId, int accountId);

    @Query("Select case When :remoteId In (Select remoteId From Folder Where account_id = :accountId) Then 1 else 0 end")
    boolean remoteFolderExists(int remoteId, int accountId);

    @Query("Select * from Folder Where id = :folderId")
    Folder select(int folderId);

    @Query("Select remoteId From Folder Where account_id = :accountId")
    List<Long> getFolderRemoteIdsOfAccount(int accountId);

    @Query("Delete From Folder Where id in (:ids)")
    void deleteByIds(List<Long> ids);
}
