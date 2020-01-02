package com.readrops.readropsdb.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Embedded;

import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;

public class FeedWithFolder implements Parcelable {

    @Embedded(prefix = "feed_")
    private Feed feed;

    @Embedded(prefix = "folder_")
    private Folder folder;

    public FeedWithFolder() {

    }

    protected FeedWithFolder(Parcel in) {
        feed = in.readParcelable(Feed.class.getClassLoader());
        folder = in.readParcelable(Folder.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(feed, flags);
        dest.writeParcelable(folder, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FeedWithFolder> CREATOR = new Creator<FeedWithFolder>() {
        @Override
        public FeedWithFolder createFromParcel(Parcel in) {
            return new FeedWithFolder(in);
        }

        @Override
        public FeedWithFolder[] newArray(int size) {
            return new FeedWithFolder[size];
        }
    };

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
