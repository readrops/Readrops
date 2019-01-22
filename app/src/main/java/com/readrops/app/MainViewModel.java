package com.readrops.app;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.entities.Item;

import java.util.List;

public class MainViewModel extends AndroidViewModel implements SimpleCallback {

    private LiveData<List<Item>> items;
    private LocalFeedRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalFeedRepository(application);
        repository.setCallback(this);
        items = repository.getItems();
    }

    public LiveData<List<Item>> getItems() {
        return items;
    }

    public void sync() {
        repository.sync();
    }


    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(Exception ex) {

    }
}
