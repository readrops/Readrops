package com.readrops.app.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.repositories.LocalFeedRepository;
import com.readrops.app.views.SimpleCallback;
import com.readrops.readropslibrary.ParsingResult;

import java.util.List;

import io.reactivex.Completable;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<ItemWithFeed>> itemsWithFeed;
    private LocalFeedRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalFeedRepository(application);

        itemsWithFeed = repository.getItemsWithFeed();
    }

    public LiveData<List<ItemWithFeed>> getItemsWithFeed() {
        return itemsWithFeed;
    }

    public void setSimpleCallback(SimpleCallback simpleCallback) {
        repository.setCallback(simpleCallback);
    }

    public Completable sync() {
        return repository.sync();
    }

    public void addFeed(ParsingResult parsingResult) {
        repository.addFeed(parsingResult);
    }
}
