package com.readrops.app;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.pojo.ItemWithFeed;

public class ItemViewModel extends AndroidViewModel {

    BasedRepository repository;

    public ItemViewModel(@NonNull Application application) {
        super(application);

        repository = new BasedRepository(application);
    }

    public LiveData<ItemWithFeed> getItemById(int id) {
        return repository.getItemById(id);
    }


}
