package com.readrops.app.utils.matchers;

import com.readrops.readropsdb.entities.Feed;
import com.readrops.api.localfeed.atom.ATOMFeed;
import com.readrops.api.localfeed.json.JSONFeed;
import com.readrops.api.localfeed.rss.RSSChannel;
import com.readrops.api.localfeed.rss.RSSFeed;

import org.jsoup.Jsoup;

public final class FeedMatcher {
    
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
}
