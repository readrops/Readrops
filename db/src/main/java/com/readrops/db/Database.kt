package com.readrops.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.readrops.db.dao.*
import com.readrops.db.entities.*
import com.readrops.db.entities.account.Account
import dev.matrix.roomigrant.GenerateRoomMigrations

@Database(entities = [Feed::class, Item::class, Folder::class, Account::class, UnreadItemsIds::class, ReadStarStateChange::class, StarredItem::class], version = 3)
@TypeConverters(Converters::class)
@GenerateRoomMigrations
abstract class Database : RoomDatabase() {
    abstract fun feedDao(): FeedDao

    abstract fun itemDao(): ItemDao

    abstract fun folderDao(): FolderDao

    abstract fun accountDao(): AccountDao

    abstract fun itemsIdsDao(): ItemsIdsDao

    abstract fun starredItemDao(): StarredItemDao
}