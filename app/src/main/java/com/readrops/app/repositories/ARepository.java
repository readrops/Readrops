package com.readrops.app.repositories;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Patterns;

import androidx.palette.graphics.Palette;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
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

public abstract class ARepository {

    protected Database database;

    protected ARepository(Application application) {
        this.database = Database.getInstance(application);
    }

    public abstract Single<Boolean> login(Account account);

    public abstract Observable<Feed> sync(List<Feed> feeds, Account account);

    public abstract Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results, Account account);

    public abstract Completable updateFeed(Feed feed, Account account);

    public abstract Completable deleteFeed(Feed feed, Account account);

    public abstract Completable addFolder(Folder folder, Account account);

    public abstract Completable updateFolder(Folder folder, Account account);

    public abstract Completable deleteFolder(Folder folder, Account account);

    public Single<Integer> getFeedCount(int accountId) {
        return Single.create(emitter -> emitter.onSuccess(database.feedDao().getFeedCount(accountId)));
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
}
