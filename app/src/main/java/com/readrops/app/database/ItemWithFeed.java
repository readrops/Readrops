package com.readrops.app.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;

import com.readrops.app.database.entities.Item;

public class ItemWithFeed {

    @Embedded
    private Item item;

    @ColumnInfo(name = "name")
    private String feedName;

    private int color;

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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
