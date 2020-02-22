package com.readrops.readropsdb.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.readrops.readropsdb.entities.account.Account;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface AccountDao extends BaseDao<Account> {

    @Query("Select * from Account")
    LiveData<List<Account>> selectAllAsync();

    @Query("Select * from Account")
    List<Account> selectAll();

    @Query("Update Account set last_modified = :lastModified Where id = :accountId")
    void updateLastModified(int accountId, long lastModified);

    @Query("Update Account set current_account = 0 Where id Not In (:accountId)")
    void deselectOldCurrentAccount(int accountId);

    @Query("Update Account set current_account = 1 Where id = :accountId")
    void setCurrentAccount(int accountId);

    @Query("Select count(*) From Account Where account_type = :accountType")
    Integer getAccountCountByType(int accountType);

    @Query("Select count(*) From Account")
    Single<Integer> getAccountCount();

    @Query("Update Account set writeToken = :writeToken Where id = :accountId")
    void updateWriteToken(int accountId, String writeToken);
}
