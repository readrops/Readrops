package com.readrops.readropsdb.dao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.readropsdb.entities.NotificationPermission

@Dao
interface NotificationPermissionDao : BaseDao<NotificationPermission> {

    @Query("Select NotificationPermission.* From NotificationPermission Inner Join Feed Where Feed.id = NotificationPermission.feedId And Feed.account_id = :accountId")
    fun selectAll(accountId: Int) : List<NotificationPermission>

    @Query("Update NotificationPermission set enabled = :enabled Where id = :id")
    fun setEnableState(id: Int, enabled: Boolean)
}