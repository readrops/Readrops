package com.readrops.db.dao.newdao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.pojo.FolderWithFeed
import kotlinx.coroutines.flow.Flow

@Dao
interface NewFolderDao : NewBaseDao<Folder> {

    @RawQuery(observedEntities = [Folder::class, Feed::class, Item::class])
    fun selectFoldersAndFeeds(query: SupportSQLiteQuery): Flow<List<FolderWithFeed>>

    @Query("Select * From Folder Where account_id = :accountId")
    fun selectFolders(accountId: Int): Flow<List<Folder>>

    @Query("Select * From Folder Where name = :name And account_id = :accountId")
    suspend fun selectFolderByName(name: String, accountId: Int): Folder?
}