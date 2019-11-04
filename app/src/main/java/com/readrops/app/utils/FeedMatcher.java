package com.readrops.app.utils;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.account.Account;
import com.readrops.readropslibrary.localfeed.atom.ATOMFeed;
import com.readrops.readropslibrary.localfeed.json.JSONFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSChannel;
import com.readrops.readropslibrary.localfeed.rss.RSSFeed;
import com.readrops.readropslibrary.opml.model.Body;
import com.readrops.readropslibrary.opml.model.Opml;
import com.readrops.readropslibrary.opml.model.Outline;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFeed;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeed;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FeedMatcher {

    public static Feed nextNewsFeedToFeed(NextNewsFeed feed, Account account) {
        Feed newFeed = new Feed();

        newFeed.setName(feed.getTitle());
        newFeed.setUrl(feed.getUrl());
        newFeed.setSiteUrl(feed.getLink());
        newFeed.setUnreadCount(feed.getUnreadCount());

        newFeed.setFolderId(feed.getFolderId());
        newFeed.setIconUrl(feed.getFaviconLink());

        newFeed.setRemoteId(String.valueOf(feed.getId()));
        newFeed.setRemoteFolderId(String.valueOf(feed.getFolderId()));

        newFeed.setAccountId(account.getId());

        return newFeed;
    }

    public static Feed freshRSSFeedToFeed(FreshRSSFeed feed, Account account) {
        Feed newFeed = new Feed();

        newFeed.setName(feed.getTitle());
        newFeed.setUrl(feed.getUrl());
        newFeed.setSiteUrl(feed.getHtmlUrl());

        newFeed.setIconUrl(feed.getIconUrl());

        newFeed.setRemoteFolderId(feed.getCategories().get(0).getId());
        newFeed.setAccountId(account.getId());
        newFeed.setRemoteId(feed.getId());

        return newFeed;
    }

    public static Feed feedFromRSS(RSSFeed rssFeed) {
        Feed feed = new Feed();
        RSSChannel channel = rssFeed.getChannel();

        feed.setName(Jsoup.parse(channel.getTitle()).text());
        feed.setUrl(channel.getFeedUrl());
        feed.setSiteUrl(channel.getUrl());
        feed.setDescription(channel.getDescription());
        feed.setLastUpdated(channel.getLastUpdated());

        feed.setEtag(rssFeed.getEtag());
        feed.setLastModified(rssFeed.getLastModified());

        feed.setFolderId(null);

        return feed;
    }

    public static Feed feedFromATOM(ATOMFeed atomFeed) {
        Feed feed = new Feed();

        feed.setName(atomFeed.getTitle());
        feed.setDescription(atomFeed.getSubtitle());
        feed.setUrl(atomFeed.getUrl());
        feed.setSiteUrl(atomFeed.getWebsiteUrl());
        feed.setDescription(atomFeed.getSubtitle());
        feed.setLastUpdated(atomFeed.getUpdated());

        feed.setEtag(atomFeed.getEtag());
        feed.setLastModified(atomFeed.getLastModified());

        feed.setFolderId(null);

        return feed;
    }

    public static Feed feedFromJSON(JSONFeed jsonFeed) {
        Feed feed = new Feed();

        feed.setName(jsonFeed.getTitle());
        feed.setUrl(jsonFeed.getFeedUrl());
        feed.setSiteUrl(jsonFeed.getHomePageUrl());
        feed.setDescription(jsonFeed.getDescription());

        feed.setEtag(jsonFeed.getEtag());
        feed.setLastModified(jsonFeed.getLastModified());
        feed.setIconUrl(jsonFeed.getFaviconUrl());

        feed.setFolderId(null);

        return feed;
    }

    public static Map<Folder, List<Feed>> feedsAndFoldersFromOPML(Opml opml) {
        Map<Folder, List<Feed>> foldersAndFeeds = new HashMap<>();
        Body body = opml.getBody();

        for (Outline outline : body.getOutlines()) {
            Folder folder = new Folder(outline.getTitle());

            List<Feed> feeds = new ArrayList<>();
            for (Outline feedOutline : outline.getOutlines()) {
                Feed feed = new Feed();
                feed.setName(feedOutline.getTitle());
                feed.setUrl(feedOutline.getXmlUrl());
                feed.setSiteUrl(feedOutline.getHtmlUrl());

                feeds.add(feed);
            }

            foldersAndFeeds.put(folder, feeds);
        }

        return foldersAndFeeds;
    }
}
