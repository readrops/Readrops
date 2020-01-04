package com.readrops.readropslibrary.services.freshrss;

import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropsdb.entities.Item;

import java.util.ArrayList;
import java.util.List;

public class FreshRSSSyncResult {

    private List<Folder> folders;

    private List<Feed> feeds;

    private List<Item> items;

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

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
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
