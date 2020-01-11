package com.readrops.readropslibrary.services.nextcloudnews;

import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolder;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItem;

import java.util.List;

public class NextNewsSyncResult {

    private List<NextNewsFolder> folders;

    private List<Feed> feeds;

    private List<NextNewsItem> items;

    private boolean error;

    public NextNewsSyncResult() {
        // empty constructor
    }

    public List<NextNewsFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<NextNewsFolder> folders) {
        this.folders = folders;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public List<NextNewsItem> getItems() {
        return items;
    }

    public void setItems(List<NextNewsItem> items) {
        this.items = items;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
