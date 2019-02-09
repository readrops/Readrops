package com.readrops.app.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.support.annotation.ColorInt;

import com.readrops.app.database.entities.Item;

public class ItemWithFeed {

    @Embedded
    private Item item;

    @ColumnInfo(name = "name")
    private String feedName;

    @ColumnInfo(name = "text_color")
    private @ColorInt int color;

    @ColumnInfo(name = "background_color")
    private @ColorInt int bgColor;

    @ColumnInfo(name = "icon_url")
    private String feedIconUrl;

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


}
