package com.readrops.app.repositories;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;
import com.readrops.app.database.entities.account.Account;
import com.readrops.app.database.entities.account.AccountType;
import com.readrops.app.utils.feedscolors.FeedsColorsIntentService;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.feedscolors.FeedColorsKt;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public abstract class ARepository<T> {

    protected Application application;
    protected Database database;
    protected Account account;

    protected T api;

    protected ARepository(@NonNull Application application, @Nullable Account account) {
        this.application = application;
        this.database = Database.getInstance(application);
        this.account = account;

        api = createAPI();
    }

    protected abstract T createAPI();

    public abstract Single<Boolean> login(Account account, boolean insert);

    public abstract Observable<Feed> sync(List<Feed> feeds);

    public abstract Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results);

    public Completable updateFeed(Feed feed) {
        return Completable.create(emitter -> {
            database.feedDao().updateFeedFields(feed.getId(), feed.getName(), feed.getUrl(), feed.getFolderId());
            emitter.onComplete();
        });
    }

    public Completable deleteFeed(Feed feed) {
        return database.feedDao().delete(feed);
    }

    public Completable addFolder(Folder folder) {
        return Completable.create(emitter -> {
            database.folderDao().insert(folder);
            emitter.onComplete();
        });
    }

    public Completable updateFolder(Folder folder) {
        return database.folderDao().update(folder);
    }

    public Completable deleteFolder(Folder folder) {
        return database.folderDao().delete(folder);
    }

    public Completable setItemReadState(Item item, boolean read) {
        return database.itemDao().setReadState(item.getId(), read ? 1 : 0, !item.isReadChanged() ? 1 : 0);
    }

    public Completable setAllItemsReadState(boolean read) {
        return database.itemDao().setAllItemsReadState(read ? 1 : 0, account.getId());
    }

    public Completable setAllFeedItemsReadState(int feedId, boolean read) {
        return database.itemDao().setAllFeedItemsReadState(feedId, read ? 1 : 0);
    }

    public Single<Integer> getFeedCount(int accountId) {
        return database.feedDao().getFeedCount(accountId);
    }

    protected void setFeedColors(Feed feed) {
        FeedColorsKt.setFeedColors(feed);
        database.feedDao().updateColors(feed.getId(),
                feed.getTextColor(), feed.getBackgroundColor());
    }

    protected void setFeedsColors(List<Feed> feeds) {
        Intent intent = new Intent(application, FeedsColorsIntentService.class);
        intent.putParcelableArrayListExtra(FeedsColorsIntentService.FEEDS, new ArrayList<>(feeds));

        application.startService(intent);
    }

    public static ARepository repositoryFactory(Account account, AccountType accountType, Application application) throws Exception {
        switch (accountType) {
            case LOCAL:
                return new LocalFeedRepository(application, account);
            case NEXTCLOUD_NEWS:
                return new NextNewsRepository(application, account);
            case FRESHRSS:
                return new FreshRSSRepository(application, account);
            default:
                throw new Exception("account type not supported");
        }
    }

    public static ARepository repositoryFactory(Account account, Application application) throws Exception {
        return ARepository.repositoryFactory(account, account.getAccountType(), application);
    }
}
