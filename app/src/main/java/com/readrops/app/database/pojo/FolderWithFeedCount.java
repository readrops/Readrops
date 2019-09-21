package com.readrops.app.database.pojo;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.readrops.app.database.entities.Folder;

public class FolderWithFeedCount {

    @Embedded
    private Folder folder;

    @ColumnInfo(name = "feed_count")
    private int feedCount;

    public FolderWithFeedCount() {
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public int getFeedCount() {
        return feedCount;
    }

    public void setFeedCount(int feedCount) {
        this.feedCount = feedCount;
    }
}
