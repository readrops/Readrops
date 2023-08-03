package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface NewAccountDao {

    @Query("Select Count(*) From Account")
    suspend fun selectAccountCount(): Int

}