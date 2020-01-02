package com.readrops.readropsdb.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.readrops.readropsdb.entities.account.Account;

import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class AccountDao implements BaseDao<Account> {

    @Query("Select * from Account")
    public abstract LiveData<List<Account>> selectAll();

    @Query("Update Account set last_modified = :lastModified Where id = :accountId")
    public abstract void updateLastModified(int accountId, long lastModified);

    @Query("Update Account set current_account = 0 Where id Not In (:accountId)")
    public abstract void deselectOldCurrentAccount(int accountId);

    @Query("Update Account set current_account = 1 Where id = :accountId")
    public abstract void setCurrentAccount(int accountId);

    @Query("Select count(*) From Account Where account_type = :accountType")
    public abstract Integer getAccountCountByType(int accountType);

    @Query("Select count(*) From Account")
    public abstract Single<Integer> getAccountCount();

    @Query("Update Account set writeToken = :writeToken Where id = :accountId")
    public abstract void updateWriteToken(int accountId, String writeToken);
}
