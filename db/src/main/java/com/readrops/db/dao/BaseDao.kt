package com.readrops.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Single

interface BaseDao<T> {

    @Insert
    fun insert(entity: T): Single<Long>

    // only here for compatibility with LocalFeedRepository
    // which hasn't been written with rxjava usage in mind
    @Insert
    fun compatInsert(entity: T): Long

    @Insert
    fun insert(entities: List<T>): List<Long>

    @Update
    fun update(entity: T): Completable

    @Update
    fun update(entities: List<T>): Completable

    @Delete
    fun delete(entity: T): Completable

    @Delete
    fun delete(entities: List<T>): Completable
}