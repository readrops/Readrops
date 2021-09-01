package com.readrops.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.account.Account
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface AccountDao : BaseDao<Account> {

    @Query("Select * from Account")
    fun selectAllAsync(): LiveData<List<Account>>

    @Query("Select * From Account Where id = :accountId")
    fun selectAsync(accountId: Int): LiveData<Account>

    @Query("Select * from Account")
    fun selectAll(): List<Account>

    @Query("Select * From Account Where id = :accountId")
    fun select(accountId: Int): Account

    @Query("Update Account set last_modified = :lastModified Where id = :accountId")
    fun updateLastModified(accountId: Int, lastModified: Long)

    @Query("Update Account set current_account = 0 Where id Not In (:accountId)")
    fun deselectOldCurrentAccount(accountId: Int)

    @Query("Update Account set current_account = 1 Where id = :accountId")
    fun setCurrentAccount(accountId: Int)

    @get:Query("Select count(*) From Account")
    val accountCount: Single<Int>

    @Query("Update Account set writeToken = :writeToken Where id = :accountId")
    fun updateWriteToken(accountId: Int, writeToken: String)

    @Query("Update Account set notifications_enabled = :enabled Where id = :accountId")
    fun updateNotificationState(accountId: Int, enabled: Boolean): Completable
}