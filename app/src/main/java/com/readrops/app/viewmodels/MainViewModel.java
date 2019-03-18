package com.readrops.app.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.repositories.LocalFeedRepository;
import com.readrops.app.utils.ParsingResult;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

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

    public Observable<Feed> sync(List<Feed> feeds) {
        return repository.sync(feeds);
    }

    public void addFeed(ParsingResult parsingResult) {
        repository.addFeed(parsingResult);
    }

    public Single<Integer> getFeedCount() {
        return repository.getFeedCount();
    }
}
