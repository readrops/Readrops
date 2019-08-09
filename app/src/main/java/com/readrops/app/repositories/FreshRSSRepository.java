package com.readrops.app.repositories;

import android.app.Application;

import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.readropslibrary.services.freshrss.FreshRSSAPI;
import com.readrops.readropslibrary.services.freshrss.FreshRSSCredentials;
import com.readrops.readropslibrary.services.freshrss.FreshRSSService;

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
        return null;
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
}
