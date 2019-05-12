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

    @Query("Select * from Folder Order By name ASC")
    LiveData<List<Folder>> getAllFolders();

    @Query("Select * from Folder Order By name ASC")
    List<Folder> getFolders();

    @Insert
    long insert(Folder folder);

    @Insert
    long[] insert(List<Folder> folders);

    @Delete
    void delete(Folder folder);

    @Query("Select id From Folder Where remoteId = :remoteId")
    int getRemoteFolderLocalId(int remoteId);

    @Query("Select case When :remoteId In (Select remoteId from Folder) Then 1 else 0 end")
    boolean remoteFolderExists(int remoteId);
}
