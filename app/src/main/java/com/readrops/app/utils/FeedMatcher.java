package com.readrops.app.utils;

import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
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

        newFeed.setRemoteId(feed.getId());
        newFeed.setAccountId(account.getId());

        return newFeed;
    }

}
