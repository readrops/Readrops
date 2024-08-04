package com.readrops.db

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
        ItemStateChange::class, ItemState::class],
    version = 4
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    abstract fun feedDao(): FeedDao

    abstract fun itemDao(): ItemDao

    abstract fun accountDao(): AccountDao

    abstract fun folderDao(): FolderDao

    abstract fun itemStateDao(): ItemStateDao

    abstract fun itemStateChangeDao(): ItemStateChangeDao
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

        // removing read_changed and adding starred fields
        db.execSQL("""CREATE TABLE IF NOT EXISTS `Item_MERGE_TABLE` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `description` TEXT, `clean_description` TEXT, `link` TEXT, `image_link` TEXT, `author` TEXT, `pub_date` INTEGER, `content` TEXT, `feed_id` INTEGER NOT NULL, `guid` TEXT, `read_time` REAL NOT NULL, `read` INTEGER NOT NULL, `starred` INTEGER NOT NULL, `read_it_later` INTEGER NOT NULL, `remoteId` TEXT, FOREIGN KEY(`feed_id`) REFERENCES `Feed`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""")
        db.execSQL("""INSERT INTO `Item_MERGE_TABLE` (`id`,`title`,`description`,`clean_description`,`link`,`image_link`,`author`,`pub_date`,`content`,`feed_id`,`guid`,`read_time`,`read`,`read_it_later`,`remoteId`,`starred`) SELECT `Item`.`id`,`Item`.`title`,`Item`.`description`,`Item`.`clean_description`,`Item`.`link`,`Item`.`image_link`,`Item`.`author`,`Item`.`pub_date`,`Item`.`content`,`Item`.`feed_id`,`Item`.`guid`,`Item`.`read_time`,`Item`.`read`,`Item`.`read_it_later`,`Item`.`remoteId`,0 FROM `Item`""")
        db.execSQL("""DROP TABLE IF EXISTS `Item`""")
        db.execSQL("""ALTER TABLE `Item_MERGE_TABLE` RENAME TO `Item`""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_Item_feed_id` ON `Item` (`feed_id`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_Item_guid` ON `Item` (`guid`)""")
    }
}

object MigrationFrom3To4 : Migration(3, 4) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // add unique index to ItemState.(account_id, remote_id)
        db.execSQL("CREATE TABLE IF NOT EXISTS `_new_ItemState` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `read` INTEGER NOT NULL, `starred` INTEGER NOT NULL, `remote_id` TEXT NOT NULL, `account_id` INTEGER NOT NULL, FOREIGN KEY(`account_id`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("INSERT INTO `_new_ItemState` (`id`,`read`,`starred`,`remote_id`,`account_id`) SELECT `id`,`read`,`starred`,`remote_id`,`account_id` FROM `ItemState`")
        db.execSQL("DROP TABLE `ItemState`")
        db.execSQL("ALTER TABLE `_new_ItemState` RENAME TO `ItemState`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ItemState_remote_id_account_id` ON `ItemState` (`remote_id`, `account_id`)")

        // remove guid, use remoteId local accounts
        db.execSQL("Update Item set remoteId = guid Where remoteId is NULL")
        db.execSQL("CREATE TABLE IF NOT EXISTS `_new_Item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `description` TEXT, `clean_description` TEXT, `link` TEXT, `image_link` TEXT, `author` TEXT, `pub_date` INTEGER, `content` TEXT, `feed_id` INTEGER NOT NULL, `read_time` REAL NOT NULL, `read` INTEGER NOT NULL, `starred` INTEGER NOT NULL, `read_it_later` INTEGER NOT NULL, `remoteId` TEXT, FOREIGN KEY(`feed_id`) REFERENCES `Feed`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("INSERT INTO `_new_Item`(`id`, `title`, `description`, `clean_description`, `link`, `image_link`, `author`, `pub_date`, `content`, `feed_id`, `read_time`, `read`, `starred`, `read_it_later`, `remoteId`) SELECT `id`, `title`, `description`, `clean_description`, `link`, `image_link`, `author`, `pub_date`, `content`, `feed_id`, `read_time`, `read`, `starred`, `read_it_later`, `remoteId` FROM `Item`")
        db.execSQL("DROP TABLE IF EXISTS `Item`")
        db.execSQL("ALTER TABLE `_new_Item` RENAME TO `Item`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Item_feed_id` ON `Item` (`feed_id`)")
    }

}
