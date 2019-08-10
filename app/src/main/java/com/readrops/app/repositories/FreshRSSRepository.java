package com.readrops.app.repositories;

import android.app.Application;

import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.readropslibrary.services.SyncType;
import com.readrops.readropslibrary.services.freshrss.FreshRSSAPI;
import com.readrops.readropslibrary.services.freshrss.FreshRSSCredentials;
import com.readrops.readropslibrary.services.freshrss.FreshRSSService;
import com.readrops.readropslibrary.services.freshrss.FreshRSSSyncData;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFeed;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItem;

import org.joda.time.LocalDateTime;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class FreshRSSRepository extends ARepository {

    public FreshRSSRepository(Application application) {
        super(application);
    }

    @Override
    public Single<Boolean> login(Account account, boolean insert) {
        FreshRSSAPI api = new FreshRSSAPI(new FreshRSSCredentials(null, account.getUrl()));

        return api.login(account.getLogin(), account.getPassword())
                .flatMap(token -> {
                    account.setToken(token);
                    api.buildAPI(new FreshRSSCredentials(token, account.getUrl()), FreshRSSService.class, FreshRSSService.END_POINT);

                    return api.getUserInfo();
                })
                .flatMap(userInfo -> {
                    account.setDisplayedName(userInfo.getUserName());

                    if (insert)
                        account.setId((int) database.accountDao().insert(account));

                    return Single.just(true);
                });
    }

    @Override
    public Observable<Feed> sync(List<Feed> feeds, Account account) {
        FreshRSSAPI api = new FreshRSSAPI(new FreshRSSCredentials(account.getToken(), account.getUrl()));

        FreshRSSSyncData syncData = new FreshRSSSyncData();
        long lastModified = LocalDateTime.now().toDateTime().getMillis();
        SyncType syncType;

        if (account.getLastModified() != 0) {
            syncType = SyncType.CLASSIC_SYNC;
            syncData.setLastModified(lastModified / 1000L);
        } else
            syncType = SyncType.INITIAL_SYNC;

        return api.sync(syncType, syncData)
                .flatMapObservable(syncResult -> {
                    insertFeeds(syncResult.getFeeds(), account);
                    insertItems(syncResult.getItems(), account);

                    return Observable.empty();
                });
    }

    @Override
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results, Account account) {
        return null;
    }

    @Override
    public Completable updateFeed(Feed feed, Account account) {
        return null;
    }

    @Override
    public Completable deleteFeed(Feed feed, Account account) {
        return null;
    }

    @Override
    public Completable addFolder(Folder folder, Account account) {
        return null;
    }

    @Override
    public Completable updateFolder(Folder folder, Account account) {
        return null;
    }

    @Override
    public Completable deleteFolder(Folder folder, Account account) {
        return null;
    }

    private void insertFeeds(List<FreshRSSFeed> feeds, Account account) {

    }

    private void insertItems(List<FreshRSSItem> items, Account account) {

    }
}
