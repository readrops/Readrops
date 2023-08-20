package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Folder

@Dao
abstract class NewFolderDao : NewBaseDao<Folder> {

    @Query("Select * From Folder Where account_id = :accountId Order By name ASC")
    abstract suspend fun selectFoldersByAccount(accountId: Int): List<Folder>


}