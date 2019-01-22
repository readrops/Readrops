package com.readrops.app.database.entities;


import android.arch.persistence.room.*;

import com.readrops.readropslibrary.localfeed.rss.RSSChannel;

@Entity
public class Feed {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    private String description;

    private String url;

    public Feed() {

    }

    public Feed(String name, String description, String url) {
        this.name = name;
        this.description = description;
        this.url = url;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Feed feedFromRSS(RSSChannel channel) {
        Feed feed = new Feed();

        feed.setUrl(channel.getLink());
        feed.setDescription(channel.getDescription());

        return feed;
    }
}
