package com.readrops.app.database.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface BaseDao<T> {

    @Insert
    Single<Long> insert(T entity); // can't turn return type to Single<Long> while some repositories can't use rxjava properly

    // only here for compatibility with LocalFeedRepository
    // which hasn't been written with rxjava usage in mind
    @Insert
    long compatInsert(T entity);

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
