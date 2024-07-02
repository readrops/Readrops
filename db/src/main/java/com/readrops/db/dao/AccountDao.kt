package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao : BaseDao<Account> {

    @Query("Select * From Account")
    fun selectAllAccounts(): Flow<List<Account>>

    @Query("Select Count(*) From Account")
    suspend fun selectAccountCount(): Int

    @Query("Select * From Account Where current_account = 1")
    fun selectCurrentAccount(): Flow<Account?>

    @Query("Delete From Account")
    suspend fun deleteAllAccounts()

    @Query("Update Account set last_modified = :lastModified Where id = :accountId")
    suspend fun updateLastModified(lastModified: Long, accountId: Int)

    @Query("Update Account set notifications_enabled = :enabled Where id = :accountId")
    suspend fun updateNotificationState(accountId: Int, enabled: Boolean)

    @Query("Select notifications_enabled From Account Where id = :accountId")
    fun selectAccountNotificationsState(accountId: Int): Flow<Boolean>
}