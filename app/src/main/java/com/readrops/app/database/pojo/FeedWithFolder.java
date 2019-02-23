package com.readrops.app.database.pojo;

import android.arch.persistence.room.Embedded;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;

public class FeedWithFolder {

    @Embedded(prefix = "feed_")
    private Feed feed;

    @Embedded(prefix = "folder_")
    private Folder folder;

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }
}
