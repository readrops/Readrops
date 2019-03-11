package com.readrops.readropslibrary.localfeed;

public class RSSQueryResult {

    private AFeed feed;

    private RSSQuery.RSSType rssType;

    private Exception exception;

    public RSSQueryResult(Exception exception) {
        this.exception = exception;
    }

    public RSSQueryResult() {

    }

    public AFeed getFeed() {
        return feed;
    }

    public void setFeed(AFeed feed) {
        this.feed = feed;
    }

    public RSSQuery.RSSType getRssType() {
        return rssType;
    }

    public void setRssType(RSSQuery.RSSType rssType) {
        this.rssType = rssType;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
