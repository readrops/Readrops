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
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Completable;
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

    public Single<Integer> getFeedCount() {
        return repository.getFeedCount();
    }

    public Single<Map<Folder, List<Feed>>> getFoldersWithFeeds() {
        return Single.create(emitter -> {
            List<Folder> folders = db.folderDao().getFolders();
            Map<Folder, List<Feed>> foldersWithFeeds = new TreeMap<>(Folder::compareTo);

            for (Folder folder : folders) {
                List<Feed> feeds = db.feedDao().getFeedsByFolder(folder.getId());

                for (Feed feed : feeds) {
                    int unreadCount = db.itemDao().getUnreadCount(feed.getId());
                    feed.setUnreadCount(unreadCount);
                }

                foldersWithFeeds.put(folder, feeds);
            }

            emitter.onSuccess(foldersWithFeeds);
        });
    }

    public Completable setItemRead(int itemId) {
        return Completable.create(emitter -> {
            db.itemDao().setRead(itemId);
            emitter.onComplete();
        });
    }
}
