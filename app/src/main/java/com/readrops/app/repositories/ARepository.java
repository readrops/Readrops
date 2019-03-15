package com.readrops.app.repositories;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.graphics.Palette;
import android.util.Patterns;

import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.HtmlParser;
import com.readrops.app.utils.Utils;
import com.readrops.app.views.SimpleCallback;
import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.utils.ParsingResult;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public abstract class ARepository {

    protected Executor executor;
    protected SimpleCallback callback;

    protected Database database;

    protected ARepository(Application application) {
        executor = Executors.newSingleThreadExecutor();
        this.database = Database.getInstance(application);
    }

    public void setCallback(SimpleCallback callback) {
        this.callback = callback;
    }

    public abstract Observable<Feed> sync(List<Feed> feeds);

    public abstract void addFeed(ParsingResult result);

    public abstract Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results);

    public abstract void updateFeed(Feed feed);

    public abstract void updateFeedWithFolder(FeedWithFolder feedWithFolder);

    public abstract Completable deleteFeed(int feedId);

    public abstract Completable addFolder(Folder folder);

    public Completable deleteFolder(Folder folder) {
        return Completable.create(emitter -> {
            database.folderDao().delete(folder);
            emitter.onComplete();
        });
    }

    public Completable changeFeedFolder(Feed feed, Folder newFolder) {
        return Completable.create(emitter -> {
            database.feedDao().updateFeedFolder(feed.getId(), newFolder.getId());
            emitter.onComplete();
        });

    }

    public Single<Integer> getFeedCount() {
        return Single.create(emitter -> {
           emitter.onSuccess(database.feedDao().getFeedCount());
        });
    }

    protected void setFavIconUtils(Feed feed) throws IOException {
        String favUrl = HtmlParser.getFaviconLink(feed.getSiteUrl());
        if (favUrl != null && Patterns.WEB_URL.matcher(favUrl).matches()) {
            feed.setIconUrl(favUrl);
            setFeedColors(favUrl, feed);
        }
    }

    protected void setFeedColors(String favUrl, Feed feed) throws IOException {
        Bitmap favicon = Utils.getImageFromUrl(favUrl);
        Palette palette = Palette.from(favicon).generate();

        feed.setTextColor(palette.getDominantSwatch().getRgb());

        if (palette.getMutedSwatch() != null)
            feed.setBackgroundColor(palette.getMutedSwatch().getRgb());
    }

    protected void failureCallBackInMainThread(Exception e) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> callback.onFailure(e));
    }

    protected void postCallBackSuccess() {
        // we go back to the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> callback.onSuccess());
    }
}
