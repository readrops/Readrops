package com.readrops.api.services.freshrss;

import java.util.ArrayList;
import java.util.List;

public class FreshRSSSyncData {

    private long lastModified;

    private List<String> readItemsIds;

    private List<String> unreadItemsIds;

    private List<String> starredItemsIds;

    private List<String> unstarredItemsIds;

    public FreshRSSSyncData() {
        readItemsIds = new ArrayList<>();
        unreadItemsIds = new ArrayList<>();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public List<String> getReadItemsIds() {
        return readItemsIds;
    }

    public void setReadItemsIds(List<String> readItemsIds) {
        this.readItemsIds = readItemsIds;
    }

    public List<String> getUnreadItemsIds() {
        return unreadItemsIds;
    }

    public void setUnreadItemsIds(List<String> unreadItemsIds) {
        this.unreadItemsIds = unreadItemsIds;
    }

    public List<String> getStarredItemsIds() {
        return starredItemsIds;
    }

    public void setStarredItemsIds(List<String> starredItemsIds) {
        this.starredItemsIds = starredItemsIds;
    }

    public List<String> getUnstarredItemsIds() {
        return unstarredItemsIds;
    }

    public void setUnstarredItemsIds(List<String> unstarredItemsIds) {
        this.unstarredItemsIds = unstarredItemsIds;
    }
}
