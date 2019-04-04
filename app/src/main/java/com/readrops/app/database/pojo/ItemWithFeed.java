package com.readrops.app.database.pojo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.support.annotation.ColorInt;

import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;

public class ItemWithFeed {

    @Embedded
    private Item item;

    @ColumnInfo(name = "name")
    private String feedName;

    @ColumnInfo(name = "feedId")
    private int feedId;

    @ColumnInfo(name = "text_color")
    private @ColorInt int color;

    @ColumnInfo(name = "background_color")
    private @ColorInt int bgColor;

    @ColumnInfo(name = "icon_url")
    private String feedIconUrl;

    @ColumnInfo(name = "siteUrl")
    private String websiteUrl;

    @Embedded(prefix = "folder_")
    private Folder folder;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public @ColorInt int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    public String getFeedIconUrl() {
        return feedIconUrl;
    }

    public void setFeedIconUrl(String feedIconUrl) {
        this.feedIconUrl = feedIconUrl;
    }

    public @ColorInt int getBgColor() {
        return bgColor;
    }

    public void setBgColor(@ColorInt int bgColor) {
        this.bgColor = bgColor;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
}
