package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.readrops.db.entities.ReadStarStateChange
import com.readrops.db.entities.UnreadItemsIds

@Dao
interface ItemsIdsDao {

    @Insert
    fun insertUnreadItemsIds(unreadItemsIds: List<UnreadItemsIds>)

    @Insert
    fun insertReadStarStateChange(readStarStateChange: ReadStarStateChange)

    @Query("Delete From UnreadItemsIds Where account_id = :accountId")
    fun deleteUnreadItemsIds(accountId: Int)

    @Query("Delete From ReadStarStateChange")
    fun deleteReadStarStateChanges()


}