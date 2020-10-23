package com.readrops.app.itemslist;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.readrops.app.repositories.ARepository;
import com.readrops.db.Database;
import com.readrops.db.ItemsListQueryBuilder;
import com.readrops.db.RoomFactoryWrapper;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.account.Account;
import com.readrops.db.filters.FilterType;
import com.readrops.db.filters.ListSortType;
import com.readrops.db.pojo.ItemWithFeed;

import org.koin.core.parameter.DefinitionParametersKt;
import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends ViewModel {

    private final MediatorLiveData<PagedList<ItemWithFeed>> itemsWithFeed;
    private LiveData<PagedList<ItemWithFeed>> lastFetch;
    private ARepository repository;
    private final Database database;

    private final ItemsListQueryBuilder queryBuilder;

    private Account currentAccount;
    private List<Account> accounts;

    public MainViewModel(@NonNull Database database) {
        queryBuilder = new ItemsListQueryBuilder();

        queryBuilder.setFilterType(FilterType.NO_FILTER);
        queryBuilder.setSortType(ListSortType.NEWEST_TO_OLDEST);

        this.database = database;
        itemsWithFeed = new MediatorLiveData<>();
    }

    //region main query

    private void setRepository() {
        repository = KoinJavaComponent.get(ARepository.class, null,
                () -> DefinitionParametersKt.parametersOf(currentAccount));
    }

    private void buildPagedList() {
        if (lastFetch != null)
            itemsWithFeed.removeSource(lastFetch);

        lastFetch = new LivePagedListBuilder<>(new RoomFactoryWrapper<>(database.itemDao().selectAll(queryBuilder.getQuery())),
                new PagedList.Config.Builder()
                        .setPageSize(100)
                        .setPrefetchDistance(150)
                        .setEnablePlaceholders(false)
                        .build())
                .build();

        itemsWithFeed.addSource(lastFetch, itemsWithFeed::setValue);
    }

    public void invalidate() {
        buildPagedList();
    }

    public void setShowReadItems(boolean showReadItems) {
        queryBuilder.setShowReadItems(showReadItems);
    }

    public boolean showReadItems() {
        return queryBuilder.showReadItems();
    }

    public void setFilterType(FilterType filterType) {
        queryBuilder.setFilterType(filterType);
    }

    public FilterType getFilterType() {
        return queryBuilder.getFilterType();
    }

    public void setSortType(ListSortType sortType) {
        queryBuilder.setSortType(sortType);
    }

    public ListSortType getSortType() {
        return queryBuilder.getSortType();
    }

    public void setFilterFeedId(int filterFeedId) {
        queryBuilder.setFilterFeedId(filterFeedId);
    }

    public MediatorLiveData<PagedList<ItemWithFeed>> getItemsWithFeed() {
        return itemsWithFeed;
    }

    public Observable<Feed> sync(List<Feed> feeds) {
        return repository.sync(feeds);
    }

    public Single<Integer> getFeedCount() {
        return repository.getFeedCount(currentAccount.getId());
    }

    public Single<Map<Folder, List<Feed>>> getFoldersWithFeeds() {
        return repository.getFoldersWithFeeds();
    }

    //endregion

    //region Account

    public LiveData<List<Account>> getAllAccounts() {
        return database.accountDao().selectAllAsync();
    }

    private Completable deselectOldCurrentAccount(int accountId) {
        return Completable.create(emitter -> {
            database.accountDao().deselectOldCurrentAccount(accountId);
            emitter.onComplete();
        });
    }

    private Account getAccount(int id) {
        for (Account account : accounts) {
            if (account.getId() == id)
                return account;
        }

        return null;
    }

    public void addAccount(Account account) {
        accounts.add(account);
        setCurrentAccount(account);
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
        setRepository();
        queryBuilder.setAccountId(currentAccount.getId());
        buildPagedList();

        // set the new account as the current one
        Completable setCurrentAccount = Completable.create(emitter -> {
            database.accountDao().setCurrentAccount(currentAccount.getId());
            emitter.onComplete();
        });

        Completable.concat(Arrays.asList(setCurrentAccount, deselectOldCurrentAccount(currentAccount.getId())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void setCurrentAccount(int id) {
        setCurrentAccount(getAccount(id));
    }


    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;

        boolean currentAccountExists = false;

        for (Account account1 : accounts) {
            if (account1.isCurrentAccount()) {
                currentAccount = account1;
                currentAccountExists = true;

                setRepository();
                queryBuilder.setAccountId(currentAccount.getId());
                buildPagedList();
                break;
            }
        }

        if (!currentAccountExists && !accounts.isEmpty()) {
            setCurrentAccount(accounts.get(0));
            accounts.get(0).setCurrentAccount(true);
        }
    }

    public boolean isAccountLocal() {
        return currentAccount.isLocal();
    }

    //endregion

    //region Item read state

    public Completable setItemReadState(ItemWithFeed itemWithFeed, boolean read) {
        return repository.setItemReadState(itemWithFeed.getItem(), read);
    }

    public Completable setItemReadState(int itemId, boolean read, boolean readChanged) {
        return repository.setItemReadState(itemId, read, readChanged);
    }

    public Completable setItemsReadState(List<ItemWithFeed> items, boolean read) {
        List<Completable> completableList = new ArrayList<>();

        for (ItemWithFeed itemWithFeed : items) {
            completableList.add(setItemReadState(itemWithFeed, read));
        }

        return Completable.concat(completableList);
    }

    public Completable setAllItemsReadState(boolean read) {
        if (queryBuilder.getFilterType() == FilterType.FEED_FILTER)
            return repository.setAllFeedItemsReadState(queryBuilder.getFilterFeedId(), read);
        else
            return repository.setAllItemsReadState(read);
    }

    public Completable setItemReadItLater(int itemId) {
        return database.itemDao().setReadItLater(itemId);
    }

    //endregion
}
