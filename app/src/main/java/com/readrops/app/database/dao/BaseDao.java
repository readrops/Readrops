package com.readrops.app.database.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;

public interface BaseDao<T> {

    @Insert
    long insert(T entity); // can't turn return type to Single<Long> while some repositories can't use rxjava properly

    @Insert
    List<Long> insert(List<T> entities);

    @Update
    Completable update(T entity);

    @Update
    Completable update(List<T> entities);

    @Delete
    Completable delete(T entity);

    @Delete
    Completable delete(List<T> entities);
}
