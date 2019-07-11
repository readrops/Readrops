package com.readrops.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

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

    @Delete
    void delete(Folder folder);

    @Query("Select id From Folder Where remoteId = :remoteId And account_id = :accountId")
    int getRemoteFolderLocalId(int remoteId, int accountId);

    @Query("Select case When :remoteId In (Select remoteId From Folder) And :accountId In (Select account_id From Folder)Then 1 else 0 end")
    boolean remoteFolderExists(int remoteId, int accountId);
}
