package com.readrops.db.dao.newdao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

interface NewBaseDao<T> {

    @Insert
    suspend fun insert(entity: T): Long

    @Insert
    suspend fun insert(entities: List<T>): List<Long>

    @Update
    suspend fun update(entity: T)

    @Update
    suspend fun update(entities: List<T>)

    @Delete
    suspend fun delete(entity: T)

    @Delete
    suspend fun delete(entities: List<T>)
}