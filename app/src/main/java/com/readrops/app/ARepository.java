package com.readrops.app;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.ParsingResult;
import com.readrops.readropslibrary.localfeed.RSSNetwork;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    public abstract void deleteFeed(Item item);

    public abstract void moveFeed(Item item);

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
