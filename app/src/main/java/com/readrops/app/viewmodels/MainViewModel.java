package com.readrops.app.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.repositories.LocalFeedRepository;
import com.readrops.app.utils.ParsingResult;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<ItemWithFeed>> itemsWithFeed;
    private LocalFeedRepository repository;
    private Database db;

    public MainViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalFeedRepository(application);
        itemsWithFeed = repository.getItemsWithFeed();
        db = Database.getInstance(application);
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

    public Single<HashMap<Folder, List<Feed>>> getFoldersWithFeeds() {
        return Single.create(emitter -> {
            List<Folder> folders = db.folderDao().getFolders();
            HashMap<Folder, List<Feed>> foldersWithFeeds = new HashMap<>();

            for (Folder folder : folders) {
                List<Feed> feeds = db.feedDao().getFeedsByFolder(folder.getId());
                foldersWithFeeds.put(folder, feeds);
            }

            emitter.onSuccess(foldersWithFeeds);
        });
    }
}
