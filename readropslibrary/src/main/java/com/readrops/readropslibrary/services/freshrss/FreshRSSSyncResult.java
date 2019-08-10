package com.readrops.readropslibrary.services.freshrss;

import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFeed;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItem;

import java.util.ArrayList;
import java.util.List;

public class FreshRSSSyncResult {

    private List<FreshRSSFeed> feeds;

    private List<FreshRSSItem> items;

    public FreshRSSSyncResult() {
        feeds = new ArrayList<>();
        items = new ArrayList<>();
    }

    public List<FreshRSSFeed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<FreshRSSFeed> feeds) {
        this.feeds = feeds;
    }

    public List<FreshRSSItem> getItems() {
        return items;
    }

    public void setItems(List<FreshRSSItem> items) {
        this.items = items;
    }
}
