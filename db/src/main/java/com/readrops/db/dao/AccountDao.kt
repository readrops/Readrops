package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao : BaseDao<Account> {

    override suspend fun insert(entity: Account): Long {
        val id = insertAccount(entity)
        updateCurrentAccount(id.toInt())
        return id
    }

    @Insert
    suspend fun insertAccount(entity: Account): Long

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

    @Query("""Update Account set current_account = Case When id = :accountId Then 1 
        When id Is Not :accountId Then 0 End""")
    suspend fun updateCurrentAccount(accountId: Int)
}