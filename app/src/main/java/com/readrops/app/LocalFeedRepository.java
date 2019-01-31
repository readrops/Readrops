package com.readrops.app;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.readrops.app.database.ItemWithFeed;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.HtmlParser;
import com.readrops.readropslibrary.ParsingResult;
import com.readrops.readropslibrary.QueryCallback;
import com.readrops.readropslibrary.localfeed.AFeed;
import com.readrops.readropslibrary.localfeed.RSSNetwork;
import com.readrops.readropslibrary.localfeed.atom.ATOMEntry;
import com.readrops.readropslibrary.localfeed.atom.ATOMFeed;
import com.readrops.readropslibrary.localfeed.json.JSONFeed;
import com.readrops.readropslibrary.localfeed.json.JSONItem;
import com.readrops.readropslibrary.localfeed.rss.RSSFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSItem;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class LocalFeedRepository extends ARepository implements QueryCallback {

    private static final String TAG = LocalFeedRepository.class.getSimpleName();

    private LiveData<List<ItemWithFeed>> itemsWhithFeed;

    public LocalFeedRepository(Application application) {
        super(application);

        itemsWhithFeed = database.itemDao().getAllItemWithFeeds();
    }

    public LiveData<List<ItemWithFeed>> getItemsWhithFeed() {
        return itemsWhithFeed;
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

            postCallBackSuccess();
        });
    }

    @Override
    public void addFeed(ParsingResult result) {
        executor.execute(() -> {
            try {
                RSSNetwork rssNet = new RSSNetwork();
                rssNet.request(result.getUrl(), this);

                postCallBackSuccess();
            } catch (Exception e) {
                failureCallBackInMainThread(e);
            }
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
    public void onSyncSuccess(AFeed feed, RSSNetwork.RSSType type) {
        switch (type) {
            case RSS_2:
                parseRSSItems(((RSSFeed) feed));
                break;
            case RSS_ATOM:
                parseATOMItems(((ATOMFeed) feed));
                break;
            case RSS_JSON:
                parseJSONItems(((JSONFeed) feed));
                break;
        }
    }

    @Override
    public void onSyncFailure(Exception e) {
        failureCallBackInMainThread(e);
    }

    private void parseRSSItems(RSSFeed rssFeed) {
        Feed dbFeed = database.feedDao().getFeedByUrl(rssFeed.getChannel().getFeedUrl());
        if (dbFeed == null) {
            dbFeed = Feed.feedFromRSS(rssFeed.getChannel());
            database.feedDao().insert(dbFeed);

            dbFeed.setId(database.feedDao().getFeedIdByUrl(rssFeed.getChannel().getFeedUrl()));
        }

        List<Item> dbItems = new ArrayList<>();
        try {
            dbItems = Item.itemsFromRSS(rssFeed.getChannel().getItems(), dbFeed);
        } catch (ParseException e) {
            failureCallBackInMainThread(e);
        }

        insertItems(dbItems);
    }

    private void parseATOMItems(ATOMFeed feed) {
        Feed dbFeed = database.feedDao().getFeedByUrl(feed.getLink());
        if (dbFeed == null) {
            dbFeed = Feed.feedFromATOM(feed);
            database.feedDao().insert(dbFeed);

            dbFeed.setId(database.feedDao().getFeedIdByUrl(feed.getLink()));
        }

        List<Item> dbItems = new ArrayList<>();
        try {
            dbItems = Item.itemsFromATOM(feed.getEntries(), dbFeed);
        } catch (ParseException e) {
            failureCallBackInMainThread(e);
        }

        insertItems(dbItems);
    }

    private void parseJSONItems(JSONFeed feed) {
        Feed dbFeed = database.feedDao().getFeedByUrl(feed.getFeedUrl());
        if (dbFeed == null) {
            dbFeed = Feed.feedFromJSON(feed);
            database.feedDao().insert(dbFeed);

            dbFeed.setId(database.feedDao().getFeedIdByUrl(feed.getFeedUrl()));
        }

        List<Item> dbItems = new ArrayList<>();
        try {
            dbItems = Item.itemsFromJSON(feed.getItems(), dbFeed);
        } catch (ParseException e) {
            failureCallBackInMainThread(e);
        }

        insertItems(dbItems);
    }

    private void insertItems(List<Item> items) {
        for (Item dbItem : items) {
            if (!Boolean.valueOf(database.itemDao().guidExist(dbItem.getGuid()))) {
                dbItem.setImageLink(HtmlParser.getDescImageLink(dbItem.getDescription()));

                database.itemDao().insert(dbItem);
                Log.d(TAG, "adding " + dbItem.getTitle());
            }
        }
    }

}
