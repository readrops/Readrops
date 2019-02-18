package com.readrops.app.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import com.readrops.app.database.dao.FeedDao;
import com.readrops.app.database.dao.FolderDao;
import com.readrops.app.database.dao.ItemDao;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;


@android.arch.persistence.room.Database(entities = {Feed.class, Item.class, Folder.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class Database extends RoomDatabase {

    public abstract FeedDao feedDao();

    public abstract ItemDao itemDao();

    public abstract FolderDao folderDao();

    private static Database database;

    public static Database getInstance(Context context) {
        if (database == null)
            database = Room.databaseBuilder(context, Database.class, "readrops-db").addCallback(roomCallback).build();

        return database;
    }

    public static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Folder folder = new Folder("reserved");
            new Thread(() -> database.folderDao().insert(folder)).start();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };

}