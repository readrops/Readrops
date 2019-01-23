package com.readrops.app;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.QueryCallback;
import com.readrops.readropslibrary.localfeed.AItem;
import com.readrops.readropslibrary.localfeed.RSSNetwork;
import com.readrops.readropslibrary.localfeed.atom.ATOMEntry;
import com.readrops.readropslibrary.localfeed.json.JSONItem;
import com.readrops.readropslibrary.localfeed.rss.RSSItem;

import java.util.List;

import static com.readrops.readropslibrary.localfeed.RSSNetwork.RSSType.RSS_2;
import static com.readrops.readropslibrary.localfeed.RSSNetwork.RSSType.RSS_ATOM;
import static com.readrops.readropslibrary.localfeed.RSSNetwork.RSSType.RSS_JSON;

public class LocalFeedRepository extends ARepository implements QueryCallback {

    public static final String TAG = LocalFeedRepository.class.getSimpleName();

    private LiveData<List<Item>> items;

    public LocalFeedRepository(Application application) {
        super(application);

        items = database.itemDao().getAll();
    }

    public LiveData<List<Item>> getItems() {
        return items;
    }

    @Override
    public void sync() {
        executor.execute(() -> {
            Log.d(TAG, "starting background thread");
            RSSNetwork request = new RSSNetwork();

            Log.d(TAG, "getting feed list");
            List<Feed> feeds = database.feedDao().getAllFeeds();

            for (Feed feed : feeds) {
                try {
                    Log.d(TAG, "entering RSSNetwork");
                    request.request(feed.getUrl(), this);
                } catch (Exception e) {
                    failureCallBackInMainThread(e);
                }
            }

            // we go back to the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> callback.onSuccess());
        });
    }

    @Override
    public void addFeed(Item item) {
        executor.execute(() -> {

        });
    }

    @Override
    public void deleteFeed(Item item) {

    }

    @Override
    public void moveFeed(Item item) {

    }


    @Override
    public void onSyncSuccess(List<? extends AItem> items, RSSNetwork.RSSType type, String feedUrl) {
        switch (type) {
            case RSS_2:
                List<RSSItem> rssItems = (List<RSSItem>)items;
                parseRSSItems(rssItems, feedUrl);
                break;
            case RSS_ATOM:
                List<ATOMEntry> atomItems = (List<ATOMEntry>)items;
                parseATOMItems(atomItems, feedUrl);
                break;
            case RSS_JSON:
                List<JSONItem> jsonItems =  (List<JSONItem>)items;
                parseJSONItems(jsonItems, feedUrl);
                break;
        }
    }

    @Override
    public void onSyncFailure(Exception ex) {
        failureCallBackInMainThread(ex);
    }

    private void parseRSSItems(List<RSSItem> items, String feedUrl) {
        Feed feed = database.feedDao().getFeedByUrl(feedUrl);

        List<Item> dbItems = Item.itemsFromRSS(items, feed);

        for (Item dbItem : dbItems) {
            if (!Boolean.valueOf(database.itemDao().guidExist(dbItem.getGuid()))) {
                database.itemDao().insert(dbItem);
                Log.d(TAG, "adding " + dbItem.getTitle());
            }
        }
    }

    private void parseATOMItems(List<ATOMEntry> items, String feedUrl) {
        Feed feed = database.feedDao().getFeedByUrl(feedUrl);

        List<Item> dbItems = Item.itemsFromATOM(items, feed);

        for (Item dbItem : dbItems) {
            if (!Boolean.valueOf(database.itemDao().guidExist(dbItem.getGuid())))
                database.itemDao().insert(dbItem);
        }
    }

    private void parseJSONItems(List<JSONItem> items, String feedUrl) {
        Feed feed = database.feedDao().getFeedByUrl(feedUrl);

        List<Item> dbItems = Item.itemsFromJSON(items, feed);

        for (Item dbItem : dbItems) {
            if (!Boolean.valueOf(database.itemDao().guidExist(dbItem.getGuid())))
                database.itemDao().insert(dbItem);
        }
    }

}
