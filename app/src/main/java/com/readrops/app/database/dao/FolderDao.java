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

    @Delete
    void delete(Folder folder);
}
