package com.readrops.app.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.util.Patterns;

import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Item;
import com.readrops.app.utils.Utils;
import com.readrops.app.utils.HtmlParser;
import com.readrops.app.utils.ParsingResult;
import com.readrops.readropslibrary.QueryCallback;
import com.readrops.readropslibrary.Utils.LibUtils;
import com.readrops.readropslibrary.localfeed.AFeed;
import com.readrops.readropslibrary.localfeed.RSSNetwork;
import com.readrops.readropslibrary.localfeed.atom.ATOMFeed;
import com.readrops.readropslibrary.localfeed.json.JSONFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSFeed;

import org.joda.time.LocalDateTime;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import io.reactivex.Completable;

public class LocalFeedRepository extends ARepository implements QueryCallback {

    private static final String TAG = LocalFeedRepository.class.getSimpleName();

    private LiveData<List<ItemWithFeed>> itemsWhithFeed;

    public LocalFeedRepository(Application application) {
        super(application);

        itemsWhithFeed = database.itemDao().getAllItemWithFeeds();
    }

    public LiveData<List<ItemWithFeed>> getItemsWithFeed() {
        return itemsWhithFeed;
    }

    @Override
    public Completable sync() {
        return Completable.create(emitter -> {
            RSSNetwork rssNet = new RSSNetwork();
            rssNet.setCallback(this);
            List<Feed> feedList = database.feedDao().getAllFeeds();

            for (Feed feed : feedList) {
                try {
                    HashMap<String, String> headers = new HashMap<>();
                    if (feed.getEtag() != null)
                        headers.put(LibUtils.IF_NONE_MATCH_HEADER, feed.getEtag());
                    if (feed.getLastModified() != null)
                        headers.put(LibUtils.IF_MODIFIED_HEADER, feed.getLastModified());

                    rssNet.requestUrl(feed.getUrl(), headers);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }

            emitter.onComplete();
        });
    }

    @Override
    public void addFeed(ParsingResult result) {
        executor.execute(() -> {
            try {
                RSSNetwork rssNet = new RSSNetwork();
                rssNet.setCallback(this);
                rssNet.requestUrl(result.getUrl(), new HashMap<>());

                postCallBackSuccess();
            } catch (Exception e) {
                failureCallBackInMainThread(e);
            }
        });
    }

    @Override
    public Completable addFeeds(List<ParsingResult> results) {
        return Completable.create(emitter -> {
           for (ParsingResult result : results) {
               RSSNetwork rssNet = new RSSNetwork();
               rssNet.setCallback(this);
               rssNet.requestUrl(result.getUrl(), new HashMap<>());
           }

            emitter.onComplete();
        });
    }

    @Override
    public void updateFeed(Feed feed) {
        executor.execute(() -> {
            try {
                database.feedDao().update(feed);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void updateFeedWithFolder(FeedWithFolder feedWithFolder) {
        executor.execute(() -> {
            Feed feed = feedWithFolder.getFeed();
            database.feedDao().updateFeedFields(feed.getId(), feed.getName(), feed.getUrl(), feed.getFolderId());
        });
    }

    @Override
    public Completable deleteFeed(int feedId) {
        return Completable.create(emitter -> {
            database.feedDao().delete(feedId);
            emitter.onComplete();
        });
    }

    @Override
    public Completable addFolder(Folder folder) {
        return Completable.create(emitter -> {
            database.folderDao().insert(folder);
            emitter.onComplete();
        });
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
        try {
            Feed dbFeed = database.feedDao().getFeedByUrl(rssFeed.getChannel().getFeedUrl());
            if (dbFeed == null) {
                dbFeed = Feed.feedFromRSS(rssFeed);

                setFavIconUtils(dbFeed);
                dbFeed.setId((int)(database.feedDao().insert(dbFeed)));
            } else
                database.feedDao().updateHeaders(rssFeed.getEtag(), rssFeed.getLastModified(), dbFeed.getId());

            List<Item> dbItems = Item.itemsFromRSS(rssFeed.getChannel().getItems(), dbFeed);
            TreeMap<LocalDateTime, Item> sortedItems = new TreeMap<>(LocalDateTime::compareTo);
            for (Item item : dbItems) {
                sortedItems.put(item.getPubDate(), item);
            }

            insertItems(sortedItems.values(), dbFeed);

        } catch (Exception e) {
            failureCallBackInMainThread(e);
        }

    }

    private void parseATOMItems(ATOMFeed feed) {
        try {
            Feed dbFeed = database.feedDao().getFeedByUrl(feed.getUrl());
            if (dbFeed == null) {
                dbFeed = Feed.feedFromATOM(feed);

                setFavIconUtils(dbFeed);
                dbFeed.setId((int)(database.feedDao().insert(dbFeed)));
            } else
                database.feedDao().updateHeaders(feed.getEtag(), feed.getLastModified(), dbFeed.getId());

            List<Item> dbItems = Item.itemsFromATOM(feed.getEntries(), dbFeed);
            TreeMap<LocalDateTime, Item> sortedItems = new TreeMap<>(LocalDateTime::compareTo);
            for (Item item : dbItems) {
                sortedItems.put(item.getPubDate(), item);
            }

            insertItems(sortedItems.values(), dbFeed);

        } catch (Exception e) {
            failureCallBackInMainThread(e);
        }
    }

    private void parseJSONItems(JSONFeed feed) {
        try {
            Feed dbFeed = database.feedDao().getFeedByUrl(feed.getFeedUrl());
            if (dbFeed == null) {
                dbFeed = Feed.feedFromJSON(feed);

                setFavIconUtils(dbFeed);
                dbFeed.setId((int)(database.feedDao().insert(dbFeed)));
            } else
                database.feedDao().updateHeaders(feed.getEtag(), feed.getLastModified(), dbFeed.getId());

            List<Item> dbItems = Item.itemsFromJSON(feed.getItems(), dbFeed);
            TreeMap<LocalDateTime, Item> sortedItems = new TreeMap<>(LocalDateTime::compareTo);
            for (Item item : dbItems) {
                sortedItems.put(item.getPubDate(), item);
            }

            insertItems(sortedItems.values(), dbFeed);

        } catch (Exception e) {
            failureCallBackInMainThread(e);
        }
    }

    private void insertItems(Collection<Item> items, Feed feed) {
        for (Item dbItem : items) {
            if (!Boolean.valueOf(database.itemDao().guidExist(dbItem.getGuid()))) {
                if (dbItem.getDescription() != null) {
                    dbItem.setCleanDescription(Jsoup.parse(dbItem.getDescription()).text());

                    if (dbItem.getImageLink() == null) {
                        String imageUrl = HtmlParser.getDescImageLink(dbItem.getDescription(), feed.getSiteUrl());

                        if (imageUrl != null)
                            dbItem.setImageLink(imageUrl);
                    }
                }

                // we check a second time because imageLink could have been set earlier with media:content tag value
                if (dbItem.getImageLink() != null) {
                    if (dbItem.getContent() != null) {
                        // removing cover image in content if found in description
                        dbItem.setContent(HtmlParser.deleteCoverImage(dbItem.getContent()));

                    } else if (dbItem.getDescription() != null)
                        dbItem.setDescription(HtmlParser.deleteCoverImage(dbItem.getDescription()));
                }

                if (dbItem.getContent() != null)
                    dbItem.setReadTime(Utils.readTimeFromString(Jsoup.parse(dbItem.getContent()).text()));
                else if (dbItem.getDescription() != null)
                    dbItem.setReadTime(Utils.readTimeFromString(dbItem.getCleanDescription()));

                database.itemDao().insert(dbItem);
            }
        }
    }

    private void setFavIconUtils(Feed feed) throws IOException {
        String favUrl = HtmlParser.getFaviconLink(feed.getSiteUrl());
        if (favUrl != null && Patterns.WEB_URL.matcher(favUrl).matches()) {
            feed.setIconUrl(favUrl);
            setFeedColors(favUrl, feed);
        }
    }

    private void setFeedColors(String favUrl, Feed feed) throws IOException {
        Bitmap favicon = Utils.getImageFromUrl(favUrl);
        Palette palette = Palette.from(favicon).generate();

        feed.setTextColor(palette.getDominantSwatch().getRgb());

        if (palette.getMutedSwatch() != null)
            feed.setBackgroundColor(palette.getMutedSwatch().getRgb());
    }


}
