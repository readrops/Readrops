package com.readrops.readropsdb;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.readrops.readropsdb.dao.AccountDao;
import com.readrops.readropsdb.dao.FeedDao;
import com.readrops.readropsdb.dao.FolderDao;
import com.readrops.readropsdb.dao.ItemDao;
import com.readrops.readropsdb.dao.NotificationPermissionDao;
import com.readrops.readropsdb.entities.NotificationPermission;
import com.readrops.readropsdb.entities.account.Account;
import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropsdb.entities.Item;


@androidx.room.Database(entities = {Feed.class, Item.class, Folder.class, Account.class, NotificationPermission.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class Database extends RoomDatabase {

    public abstract FeedDao feedDao();

    public abstract ItemDao itemDao();

    public abstract FolderDao folderDao();

    public abstract AccountDao accountDao();

    public abstract NotificationPermissionDao notificationPermissionDao();

    private static Database database;

    public static Database getInstance(Context context) {
        if (database == null)
            database = Room.databaseBuilder(context, Database.class, "readrops-db")
                    .build();

        return database;
    }
}