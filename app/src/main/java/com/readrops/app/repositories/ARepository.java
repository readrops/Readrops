package com.readrops.app.repositories;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.account.Account;
import com.readrops.app.database.entities.account.AccountType;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.HtmlParser;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.Utils;

import java.io.IOException;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public abstract class ARepository<T> {

    protected Application application;
    protected Database database;
    protected Account account;

    protected T api;

    protected ARepository(@NonNull Application application, @Nullable Account account) {
        this.application = application;
        this.database = Database.getInstance(application);
        this.account = account;
    }

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

    public Completable setAllItemsReadState(Boolean read) {
        return database.itemDao().setAllItemsReadState(read ? 1 : 0, account.getId());
    }

    public Completable setAllFeedItemsReadState(int feedId, boolean read) {
        return database.itemDao().setAllFeedItemsReadState(feedId, read ? 1 : 0);
    }

    public Single<Integer> getFeedCount(int accountId) {
        return database.feedDao().getFeedCount(accountId);
    }

    protected void setFaviconUtils(List<Feed> feeds) {
        Observable.<Feed>create(emitter -> {
            for (Feed feed : feeds) {
                setFavIconUtils(feed);
                emitter.onNext(feed);
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .doOnNext(feed1 -> database.feedDao().updateColors(feed1.getId(),
                        feed1.getTextColor(), feed1.getBackgroundColor()))
                .subscribe();
    }

    protected void setFavIconUtils(Feed feed) throws IOException {
        String favUrl;

        if (feed.getIconUrl() != null)
            favUrl = feed.getIconUrl();
        else
            favUrl = HtmlParser.getFaviconLink(feed.getSiteUrl());

        if (favUrl != null && Patterns.WEB_URL.matcher(favUrl).matches()) {
            feed.setIconUrl(favUrl);
            setFeedColors(favUrl, feed);
        }
    }

    protected void setFeedColors(String favUrl, Feed feed) {
        Bitmap favicon = Utils.getImageFromUrl(favUrl);

        if (favicon != null) {
            Palette palette = Palette.from(favicon).generate();

            feed.setTextColor(palette.getDominantSwatch().getRgb());

            if (palette.getMutedSwatch() != null)
                feed.setBackgroundColor(palette.getMutedSwatch().getRgb());
        }
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
