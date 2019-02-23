package com.readrops.app.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.Database;
import com.readrops.app.database.pojo.ItemWithFeed;

public class ItemViewModel extends AndroidViewModel {

    private Database db;

    public ItemViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<ItemWithFeed> getItemById(int id) {
        return db.itemDao().getItemById(id);
    }


}
