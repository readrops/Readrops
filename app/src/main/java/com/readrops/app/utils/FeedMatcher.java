package com.readrops.app.utils;

import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFeed;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeed;

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

        newFeed.setAccountId(account.getId());
        newFeed.setRemoteId(feed.getId());

        return newFeed;
    }
}
