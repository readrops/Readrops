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
import com.readrops.db.entities.account.AccountType
import com.readrops.db.util.Converters

@Database(
    entities = [Feed::class, Item::class, Folder::class, Account::class,
        ItemStateChange::class, ItemState::class],
    version = 5
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

        // remove guid, use remoteId for all accounts
        // remove read_it_later field
        // rename remoteId to remote_id
        db.execSQL("Update Item set remoteId = guid Where remoteId is NULL")
        db.execSQL("CREATE TABLE IF NOT EXISTS `_new_Item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `description` TEXT, `clean_description` TEXT, `link` TEXT, `image_link` TEXT, `author` TEXT, `pub_date` INTEGER, `content` TEXT, `feed_id` INTEGER NOT NULL, `read_time` REAL NOT NULL, `read` INTEGER NOT NULL, `starred` INTEGER NOT NULL, `remote_id` TEXT, FOREIGN KEY(`feed_id`) REFERENCES `Feed`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("INSERT INTO `_new_Item`(`id`, `title`, `description`, `clean_description`, `link`, `image_link`, `author`, `pub_date`, `content`, `feed_id`, `read_time`, `read`, `starred`, `remote_id`) SELECT `id`, `title`, `description`, `clean_description`, `link`, `image_link`, `author`, `pub_date`, `content`, `feed_id`, `read_time`, `read`, `starred`, `remoteId` FROM `Item`")
        db.execSQL("DROP TABLE IF EXISTS `Item`")
        db.execSQL("ALTER TABLE `_new_Item` RENAME TO `Item`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Item_feed_id` ON `Item` (`feed_id`)")

        // remove text_color
        // rename background_color to color
        // rename remoteId to remote_id
        // rename lastUpdated to last_updated
        db.execSQL("CREATE TABLE IF NOT EXISTS `_new_Feed` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `description` TEXT, `url` TEXT, `siteUrl` TEXT, `last_updated` TEXT, `color` INTEGER NOT NULL, `icon_url` TEXT, `etag` TEXT, `last_modified` TEXT, `folder_id` INTEGER, `remote_id` TEXT, `account_id` INTEGER NOT NULL, `notification_enabled` INTEGER NOT NULL DEFAULT 1, FOREIGN KEY(`folder_id`) REFERENCES `Folder`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`account_id`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("INSERT INTO `_new_Feed` (`id`, `name`, `description`, `url`, `siteUrl`, `last_updated`, `color`, `icon_url`, `etag`, `last_modified`, `folder_id`, `remote_id`, `account_id`, `notification_enabled`) SELECT `id`, `name`, `description`, `url`, `siteUrl`, `lastUpdated`, `background_color`, `icon_url`, `etag`, `last_modified`, `folder_id`, `remoteId`, `account_id`, `notification_enabled` FROM `Feed`")
        db.execSQL("DROP TABLE IF EXISTS `Feed`")
        db.execSQL("ALTER TABLE `_new_Feed` RENAME TO `Feed`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Feed_folder_id` ON `Feed` (`folder_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Feed_account_id` ON `Feed` (`account_id`)")
    }
}

object MigrationFrom4To5 : Migration(4, 5) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // rename account_name to name
        // rename account_type to type
        // rename writeToken to write_token
        db.execSQL("CREATE TABLE IF NOT EXISTS `_new_Account` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT, `name` TEXT, `displayed_name` TEXT, `type` TEXT, `last_modified` INTEGER NOT NULL, `current_account` INTEGER NOT NULL, `token` TEXT, `write_token` TEXT, `notifications_enabled` INTEGER NOT NULL)")
        db.execSQL("INSERT INTO `_new_Account` (`id`, `url`, `name`, `displayed_name`, `type`, `last_modified`, `current_account`, `token`, `write_token`, `notifications_enabled`) SELECT `id`, `url`, `account_name`, `displayed_name`, NULL, `last_modified`, `current_account`, `token`, `writeToken`, `notifications_enabled` FROM `Account`")

        // migrate type from INTEGER to TEXT
        val cursor = db.query("SELECT `id`, `account_type` FROM `Account`")
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val ordinal = cursor.getInt(1)

            val type = AccountType.entries[ordinal]

            db.execSQL("UPDATE `_new_Account` SET `type` = \"${type.name}\" WHERE `id` = $id")
        }

        db.execSQL("DROP TABLE IF EXISTS `Account`")
        db.execSQL("ALTER TABLE `_new_Account` RENAME TO `Account`")

        // add image_url field
        db.execSQL("""ALTER TABLE `Feed` ADD `image_url` TEXT DEFAULT NULL""")

        // add open_in field
        db.execSQL("""ALTER TABLE `Feed` ADD `open_in` TEXT DEFAULT 'LOCAL_VIEW' NOT NULL""")
    }
}
