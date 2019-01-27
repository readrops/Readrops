package com.readrops.app;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.HtmlParser;
import com.readrops.readropslibrary.QueryCallback;
import com.readrops.readropslibrary.localfeed.AItem;
import com.readrops.readropslibrary.localfeed.RSSNetwork;
import com.readrops.readropslibrary.localfeed.atom.ATOMEntry;
import com.readrops.readropslibrary.localfeed.json.JSONItem;
import com.readrops.readropslibrary.localfeed.rss.RSSItem;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.readrops.readropslibrary.localfeed.RSSNetwork.RSSType.RSS_2;
import static com.readrops.readropslibrary.localfeed.RSSNetwork.RSSType.RSS_ATOM;
import static com.readrops.readropslibrary.localfeed.RSSNetwork.RSSType.RSS_JSON;

public class LocalFeedRepository extends ARepository implements QueryCallback {

    public static final String TAG = LocalFeedRepository.class.getSimpleName();

    private LiveData<List<Item>> items;
    private List<Feed> feeds;

    public LocalFeedRepository(Application application) {
        super(application);

        items = database.itemDao().getAll();
        //feeds = database.feedDao().getAllFeeds();
    }

    public LiveData<List<Item>> getItems() {
        return items;
    }

    @Override
    public void sync() {
        executor.execute(() -> {
            RSSNetwork rssNet = new RSSNetwork();
            List<Feed> feedList = database.feedDao().getAllFeeds();

            for (Feed feed : feedList) {
                try {
                    rssNet.request(feed.getUrl(), this);
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
        executor.execute(() -> {

        });
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

        List<Item> dbItems = new ArrayList<>();
        try {
            dbItems = Item.itemsFromRSS(items, feed);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (Item dbItem : dbItems) {
            if (!Boolean.valueOf(database.itemDao().guidExist(dbItem.getGuid()))) {
                dbItem.setImageLink(HtmlParser.getDescImageLink(dbItem.getDescription()));

                database.itemDao().insert(dbItem);
                Log.d(TAG, "adding " + dbItem.getTitle());
            }
        }
    }

    private void parseATOMItems(List<ATOMEntry> items, String feedUrl) {
        Feed feed = database.feedDao().getFeedByUrl(feedUrl);

        List<Item> dbItems = new ArrayList<>();
        try {
            dbItems = Item.itemsFromATOM(items, feed);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (Item dbItem : dbItems) {
            if (!Boolean.valueOf(database.itemDao().guidExist(dbItem.getGuid())))
                database.itemDao().insert(dbItem);
        }
    }

    private void parseJSONItems(List<JSONItem> items, String feedUrl) {
        Feed feed = database.feedDao().getFeedByUrl(feedUrl);

        List<Item> dbItems = new ArrayList<>();
        try {
            dbItems = Item.itemsFromJSON(items, feed);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (Item dbItem : dbItems) {
            if (!Boolean.valueOf(database.itemDao().guidExist(dbItem.getGuid())))
                database.itemDao().insert(dbItem);
        }
    }

}
