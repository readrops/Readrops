package com.readrops.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.readrops.db.dao.AccountDao
import com.readrops.db.dao.FeedDao
import com.readrops.db.dao.FolderDao
import com.readrops.db.dao.ItemDao
import com.readrops.db.dao.ItemStateChangeDao
import com.readrops.db.dao.ItemStateDao
import com.readrops.db.dao.newdao.NewAccountDao
import com.readrops.db.dao.newdao.NewFeedDao
import com.readrops.db.dao.newdao.NewFolderDao
import com.readrops.db.dao.newdao.NewItemDao
import com.readrops.db.dao.newdao.NewItemStateChangeDao
import com.readrops.db.dao.newdao.NewItemStateDao
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.ItemStateChange
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

    abstract fun newAccountDao(): NewAccountDao

    abstract fun newFolderDao(): NewFolderDao

    abstract fun newItemStateDao(): NewItemStateDao

    abstract fun newItemStateChangeDao(): NewItemStateChangeDao
}