package com.readrops.readropslibrary.services.freshrss.json;

import java.util.List;

public class FreshRSSFeeds {

    private List<FreshRSSFeed> subscriptions;

    public List<FreshRSSFeed> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<FreshRSSFeed> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
