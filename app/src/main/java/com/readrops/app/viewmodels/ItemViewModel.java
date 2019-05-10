package com.readrops.app.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import com.readrops.app.database.Database;
import com.readrops.app.database.dao.ItemDao;
import com.readrops.app.database.pojo.ItemWithFeed;

public class ItemViewModel extends AndroidViewModel {

    private ItemDao itemDao;

    public ItemViewModel(@NonNull Application application) {
        super(application);
        itemDao = Database.getInstance(application).itemDao();
    }

    public LiveData<ItemWithFeed> getItemById(int id) {
        return itemDao.getItemById(id);
    }


}
