package com.readrops.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.readrops.db.dao.AccountDao
import com.readrops.db.dao.FeedDao
import com.readrops.db.dao.FolderDao
import com.readrops.db.dao.ItemDao
import com.readrops.db.dao.ItemStateChangeDao
import com.readrops.db.dao.ItemStateDao
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.ItemStateChange
import com.readrops.db.entities.account.Account

@Database(
    entities = [Feed::class, Item::class, Folder::class, Account::class,
        ItemStateChange::class, ItemState::class], version = 4,
    autoMigrations = [
        AutoMigration(3, 4)
    ]
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    // new dao

    abstract fun newFeedDao(): FeedDao

    abstract fun newItemDao(): ItemDao

    abstract fun newAccountDao(): AccountDao

    abstract fun newFolderDao(): FolderDao

    abstract fun newItemStateDao(): ItemStateDao

    abstract fun newItemStateChangeDao(): ItemStateChangeDao
}

object MigrationFrom1To2 : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""ALTER TABLE `Feed` ADD `notification_enabled` INTEGER NOT NULL DEFAULT 1""")
        db.execSQL("""ALTER TABLE `Account` ADD `notifications_enabled` INTEGER NOT NULL DEFAULT 0""")
    }
}

object MigrationFrom2To3 : Migration(2, 3) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // new tables for separate item read/star state management
        db.execSQL("""CREATE TABLE IF NOT EXISTS `ItemStateChange` (`id` INTEGER NOT NULL, `read_change` INTEGER NOT NULL, `star_change` INTEGER NOT NULL, `account_id` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`account_id`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS `ItemState` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `read` INTEGER NOT NULL, `starred` INTEGER NOT NULL, `remote_id` TEXT NOT NULL, `account_id` INTEGER NOT NULL, FOREIGN KEY(`account_id`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""")

        // removing read_changed and adding starred fields. Table is recreated to keep field order
        db.execSQL("""CREATE TABLE IF NOT EXISTS `Item_MERGE_TABLE` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `description` TEXT, `clean_description` TEXT, `link` TEXT, `image_link` TEXT, `author` TEXT, `pub_date` INTEGER, `content` TEXT, `feed_id` INTEGER NOT NULL, `guid` TEXT, `read_time` REAL NOT NULL, `read` INTEGER NOT NULL, `starred` INTEGER NOT NULL, `read_it_later` INTEGER NOT NULL, `remoteId` TEXT, FOREIGN KEY(`feed_id`) REFERENCES `Feed`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""")
        db.execSQL("""INSERT INTO `Item_MERGE_TABLE` (`id`,`title`,`description`,`clean_description`,`link`,`image_link`,`author`,`pub_date`,`content`,`feed_id`,`guid`,`read_time`,`read`,`read_it_later`,`remoteId`,`starred`) SELECT `Item`.`id`,`Item`.`title`,`Item`.`description`,`Item`.`clean_description`,`Item`.`link`,`Item`.`image_link`,`Item`.`author`,`Item`.`pub_date`,`Item`.`content`,`Item`.`feed_id`,`Item`.`guid`,`Item`.`read_time`,`Item`.`read`,`Item`.`read_it_later`,`Item`.`remoteId`,0 FROM `Item`""")
        db.execSQL("""DROP TABLE IF EXISTS `Item`""")
        db.execSQL("""ALTER TABLE `Item_MERGE_TABLE` RENAME TO `Item`""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_Item_feed_id` ON `Item` (`feed_id`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_Item_guid` ON `Item` (`guid`)""")
    }
}