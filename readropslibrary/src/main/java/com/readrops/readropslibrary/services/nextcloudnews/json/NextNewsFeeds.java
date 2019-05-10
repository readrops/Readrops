package com.readrops.readropslibrary.services.nextcloudnews.json;

import java.util.List;

public class NextNewsFeeds {

    private List<NextNewsFeed> feeds;

    public NextNewsFeeds() {

    }

    public List<NextNewsFeed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<NextNewsFeed> feeds) {
        this.feeds = feeds;
    }
}
