package com.readrops.app.database.entities;


import android.arch.persistence.room.*;
import android.support.annotation.ColorInt;

import com.readrops.readropslibrary.localfeed.atom.ATOMFeed;
import com.readrops.readropslibrary.localfeed.json.JSONFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSChannel;
import com.readrops.readropslibrary.localfeed.rss.RSSFeed;

import java.nio.channels.Channel;

@Entity
public class Feed {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    private String description;

    private String url;

    private String siteUrl;

    private String lastUpdated;

    private @ColorInt int color;

    @ColumnInfo(name = "icon_url")
    private String iconUrl;

    private String etag;

    @ColumnInfo(name = "last_modified")
    private String lastModified;

    public Feed() {

    }

    @Ignore
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

    public @ColorInt int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
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

    public static Feed feedFromRSS(RSSFeed rssFeed) {
        Feed feed = new Feed();
        RSSChannel channel = rssFeed.getChannel();

        feed.setName(channel.getTitle());
        feed.setUrl(channel.getFeedUrl());
        feed.setSiteUrl(channel.getUrl());
        feed.setDescription(channel.getDescription());
        feed.setLastUpdated(channel.getLastUpdated());

        feed.setEtag(rssFeed.getEtag());
        feed.setLastModified(rssFeed.getLastModified());

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

        return feed;
    }

    public static Feed feedFromJSON(JSONFeed jsonFeed) {
        Feed feed = new Feed();

        feed.setName(jsonFeed.getTitle());
        feed.setUrl(jsonFeed.getFeedUrl());
        feed.setDescription(jsonFeed.getDescription());
        //feed.setLastUpdated(jsonFeed.); maybe later ?

        feed.setEtag(jsonFeed.getEtag());
        feed.setLastModified(jsonFeed.getLastModified());

        return feed;
    }
}
