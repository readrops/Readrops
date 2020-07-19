package com.readrops.db.pojo;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.readrops.db.entities.Folder;

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
