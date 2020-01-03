package com.readrops.readropslibrary.services.freshrss;

import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItem;

import java.util.ArrayList;
import java.util.List;

public class FreshRSSSyncResult {

    private List<Folder> folders;

    private List<Feed> feeds;

    private List<FreshRSSItem> items;

    private long lastUpdated;

    public FreshRSSSyncResult() {
        feeds = new ArrayList<>();
        items = new ArrayList<>();
    }

    public List<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public List<FreshRSSItem> getItems() {
        return items;
    }

    public void setItems(List<FreshRSSItem> items) {
        this.items = items;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }
}
