package com.readrops.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.readrops.db.dao.*
import com.readrops.db.dao.newdao.NewFeedDao
import com.readrops.db.dao.newdao.NewItemDao
import com.readrops.db.entities.*
import com.readrops.db.entities.account.Account
import dev.matrix.roomigrant.GenerateRoomMigrations

@Database(entities = [Feed::class, Item::class, Folder::class, Account::class,
    ItemStateChange::class, ItemState::class], version = 3)
@TypeConverters(Converters::class)
@GenerateRoomMigrations
abstract class Database : RoomDatabase() {
    abstract fun feedDao(): FeedDao

    abstract fun itemDao(): ItemDao

    abstract fun folderDao(): FolderDao

    abstract fun accountDao(): AccountDao

    abstract fun itemStateDao(): ItemStateDao

    abstract fun itemStateChangesDao(): ItemStateChangeDao

    // new dao

    abstract fun newFeedDao(): NewFeedDao

    abstract fun newItemDao(): NewItemDao
}