package com.readrops.app.utils;

import com.readrops.app.database.entities.Feed;

public class SyncError {

    private Feed feed;

    private FeedInsertionError insertionError;

    public SyncError() {
    }

    public SyncError(Feed feed, FeedInsertionError insertionError) {
        this.feed = feed;
        this.insertionError = insertionError;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public FeedInsertionError getInsertionError() {
        return insertionError;
    }

    public void setInsertionError(FeedInsertionError insertionError) {
        this.insertionError = insertionError;
    }

    public enum FeedInsertionError {
        NETWORK_ERROR,
        DB_ERROR,
        PARSE_ERROR,
        FORMAT_ERROR
    }
}
