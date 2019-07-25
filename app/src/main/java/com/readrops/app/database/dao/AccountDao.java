package com.readrops.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.readrops.app.database.entities.Account;

import java.util.List;

@Dao
public interface AccountDao {

    @Query("Select * from Account")
    LiveData<List<Account>> selectAll();

    @Insert
    long insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("Update Account set last_modified = :lastModified Where id = :accountId")
    void updateLastModified(int accountId, long lastModified);

    @Query("Update Account set current_account = 0 Where id Not In (:accountId)")
    void deselectOldCurrentAccount(int accountId);

    @Query("Update Account set current_account = 1 Where id = :accountId")
    void setCurrentAccount(int accountId);

    @Query("Select count(*) From Account Where account_type = :accountType")
    Integer getAccountCountByType(int accountType);

    @Query("Select count(*) From Account")
    Integer getAccountCount();
}
