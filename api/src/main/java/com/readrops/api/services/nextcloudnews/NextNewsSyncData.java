package com.readrops.api.services.nextcloudnews;

import java.util.ArrayList;
import java.util.List;

public class NextNewsSyncData {

    private List<String> unreadItems;

    private List<String> readItems;

    private List<Integer> starredItems;

    private List<Integer> unstarredItems;

    private long lastModified;

    public NextNewsSyncData() {
        unreadItems = new ArrayList<>();
        readItems = new ArrayList<>();
        starredItems = new ArrayList<>();
        unstarredItems = new ArrayList<>();
    }

    public List<String> getUnreadItems() {
        return unreadItems;
    }

    public void setUnreadItems(List<String> unreadItems) {
        this.unreadItems = unreadItems;
    }

    public List<String> getReadItems() {
        return readItems;
    }

    public void setReadItems(List<String> readItems) {
        this.readItems = readItems;
    }

    public List<Integer> getStarredItems() {
        return starredItems;
    }

    public void setStarredItems(List<Integer> starredItems) {
        this.starredItems = starredItems;
    }

    public List<Integer> getUnstarredItems() {
        return unstarredItems;
    }

    public void setUnstarredItems(List<Integer> unstarredItems) {
        this.unstarredItems = unstarredItems;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
