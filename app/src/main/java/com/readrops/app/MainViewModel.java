package com.readrops.app;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import com.readrops.app.database.entities.Item;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<Item>> items;
    private LocalFeedRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalFeedRepository(application);

        items = repository.getItems();
    }

    public LiveData<List<Item>> getItems() {
        return items;
    }

    public void setSimpleCallback(SimpleCallback simpleCallback) {
        repository.setCallback(simpleCallback);
    }

    public void sync() {
        repository.sync();
    }
}
