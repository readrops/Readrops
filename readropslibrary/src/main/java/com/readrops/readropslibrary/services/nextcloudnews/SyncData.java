package com.readrops.readropslibrary.services.nextcloudnews;

import java.util.ArrayList;
import java.util.List;

public class SyncData {

    private List<Integer> unreadItems;

    private List<Integer> readItems;

    private List<Integer> starredItems;

    private List<Integer> unstarredItems;

    private long lastModified;

    public SyncData() {
        unreadItems = new ArrayList<>();
        readItems = new ArrayList<>();
        starredItems = new ArrayList<>();
        unstarredItems = new ArrayList<>();
    }

    public List<Integer> getUnreadItems() {
        return unreadItems;
    }

    public void setUnreadItems(List<Integer> unreadItems) {
        this.unreadItems = unreadItems;
    }

    public List<Integer> getReadItems() {
        return readItems;
    }

    public void setReadItems(List<Integer> readItems) {
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
