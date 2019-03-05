package com.readrops.app.repositories;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.views.SimpleCallback;
import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.readropslibrary.ParsingResult;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Completable;

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

    public abstract void sync();

    public abstract void addFeed(ParsingResult result);

    public abstract void updateFeed(Feed feed);

    public abstract void updateFeedWithFolder(FeedWithFolder feedWithFolder);

    public abstract void deleteFeed(Feed feed);

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
