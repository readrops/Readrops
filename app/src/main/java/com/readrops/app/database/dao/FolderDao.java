package com.readrops.app.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.readrops.app.database.entities.Folder;

import java.util.List;

@Dao
public interface FolderDao {

    @Query("Select * from Folder")
    List<Folder> getAllFolders();

    @Insert
    long insert(Folder folder);
}
