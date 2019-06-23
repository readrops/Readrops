package com.readrops.app.database.entities;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.readrops.readropslibrary.localfeed.atom.ATOMFeed;
import com.readrops.readropslibrary.localfeed.json.JSONFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSChannel;
import com.readrops.readropslibrary.localfeed.rss.RSSFeed;

import org.jsoup.Jsoup;

@Entity(foreignKeys = {@ForeignKey(entity = Folder.class, parentColumns = "id",
        childColumns = "folder_id", onDelete = ForeignKey.SET_NULL),
        @ForeignKey(entity = Account.class, parentColumns = "id", childColumns = "account_id",
                onDelete = ForeignKey.CASCADE)})
public class Feed implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    private String description;

    private String url;

    private String siteUrl;

    private String lastUpdated;

    @ColumnInfo(name = "text_color")
    private @ColorInt
    int textColor;

    @ColumnInfo(name = "background_color")
    private @ColorInt
    int backgroundColor;

    @ColumnInfo(name = "icon_url")
    private String iconUrl;

    private String etag;

    @ColumnInfo(name = "last_modified")
    private String lastModified;

    @ColumnInfo(name = "folder_id", index = true)
    private Integer folderId; // nullable foreign key so Integer instead of int

    private int remoteId;

    @ColumnInfo(name = "account_id", index = true)
    private int accountId;

    @Ignore
    private int unreadCount;

    public Feed() {

    }

    @Ignore
    public Feed(String name, String description, String url) {
        this.name = name;
        this.description = description;
        this.url = url;
    }

    protected Feed(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        url = in.readString();
        siteUrl = in.readString();
        lastUpdated = in.readString();
        textColor = in.readInt();
        backgroundColor = in.readInt();
        iconUrl = in.readString();
        etag = in.readString();
        lastModified = in.readString();
        folderId = in.readInt();
        remoteId = in.readInt();
    }

    public static final Creator<Feed> CREATOR = new Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel in) {
            return new Feed(in);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public @ColorInt
    int getTextColor() {
        return textColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
    }

    public @ColorInt
    int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public static Feed feedFromRSS(RSSFeed rssFeed) {
        Feed feed = new Feed();
        RSSChannel channel = rssFeed.getChannel();

        feed.setName(Jsoup.parse(channel.getTitle()).text());
        feed.setUrl(channel.getFeedUrl());
        feed.setSiteUrl(channel.getUrl());
        feed.setDescription(channel.getDescription());
        feed.setLastUpdated(channel.getLastUpdated());

        feed.setEtag(rssFeed.getEtag());
        feed.setLastModified(rssFeed.getLastModified());

        feed.setFolderId(null);

        return feed;
    }

    public static Feed feedFromATOM(ATOMFeed atomFeed) {
        Feed feed = new Feed();

        feed.setName(atomFeed.getTitle());
        feed.setDescription(atomFeed.getSubtitle());
        feed.setUrl(atomFeed.getUrl());
        feed.setSiteUrl(atomFeed.getWebsiteUrl());
        feed.setDescription(atomFeed.getSubtitle());
        feed.setLastUpdated(atomFeed.getUpdated());

        feed.setEtag(atomFeed.getEtag());
        feed.setLastModified(atomFeed.getLastModified());

        feed.setFolderId(null);

        return feed;
    }

    public static Feed feedFromJSON(JSONFeed jsonFeed) {
        Feed feed = new Feed();

        feed.setName(jsonFeed.getTitle());
        feed.setUrl(jsonFeed.getFeedUrl());
        feed.setSiteUrl(jsonFeed.getHomePageUrl());
        feed.setDescription(jsonFeed.getDescription());

        feed.setEtag(jsonFeed.getEtag());
        feed.setLastModified(jsonFeed.getLastModified());
        feed.setIconUrl(jsonFeed.getFaviconUrl());

        feed.setFolderId(null);

        return feed;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(url);
        dest.writeString(siteUrl);
        dest.writeString(lastUpdated);
        dest.writeInt(textColor);
        dest.writeInt(backgroundColor);
        dest.writeString(iconUrl);
        dest.writeString(etag);
        dest.writeString(lastModified);
        dest.writeInt(folderId);
        dest.writeInt(remoteId);
    }
}
