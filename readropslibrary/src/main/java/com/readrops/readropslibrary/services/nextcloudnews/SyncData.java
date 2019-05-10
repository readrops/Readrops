package com.readrops.readropslibrary.services.nextcloudnews;

import java.util.ArrayList;
import java.util.List;

public class SyncData {

    private List<Integer> unreadItems;

    private List<Integer> readItems;

    private List<Integer> starredItems;

    private List<Integer> unstarredItems;

    public SyncData() {
        unreadItems = new ArrayList<>();
        readItems = new ArrayList<>();
        starredItems = new ArrayList<>();
        unstarredItems = new ArrayList<>();
    }

    public void addUnreadItem(int itemId) {
        unreadItems.add(itemId);
    }

    public void addReadItem(int itemId) {
        readItems.add(itemId);
    }

    public void addStarredItem(int itemId) {
            starredItems.add(itemId);
    }

    public void addUnstarredItem(int itemId) {
        unstarredItems.add(itemId);
    }

    public List<Integer> getUnreadItems() {
        return unreadItems;
    }

    public List<Integer> getReadItems() {
        return readItems;
    }

    public List<Integer> getStarredItems() {
        return starredItems;
    }

    public List<Integer> getUnstarredItems() {
        return unstarredItems;
    }
}
