package com.readrops.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.readrops.app.activities.MainActivity;
import com.readrops.app.database.Database;
import com.readrops.app.database.ItemsListQueryBuilder;
import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.repositories.ARepository;
import com.readrops.app.repositories.NextNewsRepository;

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
    private ARepository repository;
    private Database db;

    private ItemsListQueryBuilder queryBuilder;

    public MainViewModel(@NonNull Application application) {
        super(application);

        queryBuilder = new ItemsListQueryBuilder();

        queryBuilder.setFilterType(FilterType.NO_FILTER);
        queryBuilder.setSortType(MainActivity.ListSortType.NEWEST_TO_OLDEST);

        repository = new NextNewsRepository(application);
        db = Database.getInstance(application);

        itemsWithFeed = new MediatorLiveData<>();
    }

    private void buildPagedList() {
        if (lastFetch != null)
            itemsWithFeed.removeSource(lastFetch);

        lastFetch = new LivePagedListBuilder<>(db.itemDao().selectAll(queryBuilder.getQuery()),
                new PagedList.Config.Builder()
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
        queryBuilder.setShowReaditems(showReadItems);
    }

    public boolean showReadItems() {
        return queryBuilder.showReaditems();
    }

    public void setFilterType(FilterType filterType) {
        queryBuilder.setFilterType(filterType);
    }

    public FilterType getFilterType() {
        return queryBuilder.getFilterType();
    }

    public void setSortType(MainActivity.ListSortType sortType) {
        queryBuilder.setSortType(sortType);
    }

    public MainActivity.ListSortType getSortType() {
        return queryBuilder.getSortType();
    }

    public void setFilterFeedId(int filterFeedId) {
        queryBuilder.setFilterFeedId(filterFeedId);
    }
    
    public MediatorLiveData<PagedList<ItemWithFeed>> getItemsWithFeed() {
        return itemsWithFeed;
    }

    public Observable<Feed> sync(List<Feed> feeds, Account account) {
        return repository.sync(feeds, account);
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

    public LiveData<Account> getCurrentAccount() {
        return db.accountDao().selectCurrentAccount();
    }

    public Completable setItemReadState(int itemId, boolean read, boolean readChanged) {
        return Completable.create(emitter -> {
            db.itemDao().setReadState(itemId, read ? 1 : 0, readChanged ? 1 : 0);
            emitter.onComplete();
        });
    }

    public Completable setItemsReadState(List<ItemWithFeed> items, boolean read) {
        List<Completable> completableList = new ArrayList<>();

        for (ItemWithFeed itemWithFeed : items) {
            completableList.add(setItemReadState(itemWithFeed.getItem().getId(), read,
                    !itemWithFeed.getItem().isReadChanged()));
        }

        return Completable.concat(completableList);
    }

    public Completable setAllItemsReadState(boolean read) {
        return Completable.create(emitter -> {
            if (queryBuilder.getFilterType() == FilterType.FEED_FILTER)
                db.itemDao().setAllItemsReadState(queryBuilder.getFilterFeedId(), read ? 1 : 0);
            else
                db.itemDao().setAllItemsReadState(read ? 1 : 0);
            emitter.onComplete();
        });
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
