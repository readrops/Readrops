package com.readrops.app.viewmodels;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import com.readrops.app.activities.MainActivity;
import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.repositories.LocalFeedRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class MainViewModel extends AndroidViewModel {

    private MediatorLiveData<PagedList<ItemWithFeed>> itemsWithFeed;
    private LiveData<PagedList<ItemWithFeed>> lastFetch;
    private LocalFeedRepository repository;
    private Database db;

    private boolean showReadItems;
    private FilterType filterType;
    private MainActivity.ListSortType sortType;
    private int filterFeedId;

    public MainViewModel(@NonNull Application application) {
        super(application);

        filterType = FilterType.NO_FILTER;
        sortType = MainActivity.ListSortType.NEWEST_TO_OLDEST;

        repository = new LocalFeedRepository(application);
        db = Database.getInstance(application);

        itemsWithFeed = new MediatorLiveData<>();

        buildPagedList();
    }

    private void buildPagedList() {
        DataSource.Factory<Integer, ItemWithFeed> items;
        int readState = showReadItems ? 1 : 0;

        switch (filterType) {
            case FEED_FILTER:
                if (sortType == MainActivity.ListSortType.NEWEST_TO_OLDEST)
                    items = db.itemDao().selectAllByFeedASC(filterFeedId, readState);
                else
                    items = db.itemDao().selectAllByFeedsDESC(filterFeedId, readState);
                break;
            case READ_IT_LATER_FILTER:
                if (sortType == MainActivity.ListSortType.NEWEST_TO_OLDEST)
                    items = db.itemDao().selectAllReadItLaterASC(readState);
                else
                    items = db.itemDao().selectAllReadItLaterDESC(readState);
                break;
            default:
                if (sortType == MainActivity.ListSortType.NEWEST_TO_OLDEST)
                    items = db.itemDao().selectAllASC(readState);
                else
                    items = db.itemDao().selectAllDESC(readState);
                break;
        }

        if (lastFetch != null)
            itemsWithFeed.removeSource(lastFetch);

        lastFetch = new LivePagedListBuilder<>(items, new PagedList.Config.Builder()
                .setPageSize(40)
                .setPrefetchDistance(80)
                .setEnablePlaceholders(false)
                .build())
                .build();

        itemsWithFeed.addSource(lastFetch, itemWithFeeds -> itemsWithFeed.setValue(itemWithFeeds));
    }

    public void invalidate() {
        buildPagedList();
    }

    public void setShowReadItems(boolean showReadItems) {
        this.showReadItems = showReadItems;
    }

    public boolean showReadItems() {
        return showReadItems;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setSortType(MainActivity.ListSortType sortType) {
        this.sortType = sortType;
    }

    public MainActivity.ListSortType getSortType() {
        return sortType;
    }

    public void setFilterFeedId(int filterFeedId) {
        this.filterFeedId = filterFeedId;
    }

    public MediatorLiveData<PagedList<ItemWithFeed>> getItemsWithFeed() {
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

    public Completable setItemReadState(int itemId, boolean read) {
        return Completable.create(emitter -> {
            db.itemDao().setReadState(itemId, read ? 1 : 0);
            emitter.onComplete();
        });
    }

    public Completable setItemsReadState(List<Integer> ids, boolean read) {
        List<Completable> completableList = new ArrayList<>();

        for (int id : ids) {
            completableList.add(setItemReadState(id, read));
        }

        return Completable.concat(completableList);
    }

    public Completable setItemReadItLater(int itemId) {
        return Completable.create(emitter -> {
            db.itemDao().setReadItLater(itemId);
            emitter.onComplete();
        });
    }

    public enum FilterType {
        FEED_FILTER,
        READ_IT_LATER_FILTER,
        NO_FILTER
    }
}
