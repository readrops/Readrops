package com.readrops.app.views;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.repositories.LocalFeedRepository;
import com.readrops.readropslibrary.ParsingResult;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<ItemWithFeed>> itemsWithFeed;
    private LocalFeedRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalFeedRepository(application);

        itemsWithFeed = repository.getItemsWhithFeed();
    }

    public LiveData<List<ItemWithFeed>> getItemsWithFeed() {
        return itemsWithFeed;
    }

    public void setSimpleCallback(SimpleCallback simpleCallback) {
        repository.setCallback(simpleCallback);
    }

    public void sync() {
        repository.sync();
    }

    public void addFeed(ParsingResult parsingResult) {
        repository.addFeed(parsingResult);
    }
}
