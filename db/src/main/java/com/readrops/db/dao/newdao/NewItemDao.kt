package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Item
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NewItemDao : NewBaseDao<Item> {

    @Query("Select * From Item")
    abstract fun selectAll(): Flow<List<Item>>
}