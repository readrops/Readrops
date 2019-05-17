package com.readrops.app.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.readrops.app.database.dao.AccountDao;
import com.readrops.app.database.dao.FeedDao;
import com.readrops.app.database.dao.FolderDao;
import com.readrops.app.database.dao.ItemDao;
import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;


@androidx.room.Database(entities = {Feed.class, Item.class, Folder.class, Account.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class Database extends RoomDatabase {

    public abstract FeedDao feedDao();

    public abstract ItemDao itemDao();

    public abstract FolderDao folderDao();

    public abstract AccountDao accountDao();

    private static Database database;

    public static Database getInstance(Context context) {
        if (database == null)
            database = Room.databaseBuilder(context, Database.class, "readrops-db")
                    .addCallback(roomCallback)
                    .build();

        return database;
    }

    public static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Folder folder = new Folder("reserved");
            new Thread(() -> database.folderDao().insert(folder)).start();

            Account account = new Account("", "Nextcloud News",
                    Account.AccountType.NEXTCLOUD_NEWS);

            new Thread(() -> database.accountDao().insert(account)).start();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };

}