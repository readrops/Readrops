package com.readrops.readropslibrary.localfeed.json;

import com.google.gson.annotations.SerializedName;
import com.readrops.readropslibrary.localfeed.AFeed;

import java.util.List;

public class JSONFeed extends AFeed {

    private String title;

    @SerializedName("home_page_url")
    private String homePageUrl;

    @SerializedName("feed_url")
    private String feedUrl;

    private String description;

    @SerializedName("icon")
    private String iconUrl;

    @SerializedName("favicon")
    private String faviconUrl;

    private List<JSONItem> items;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHomePageUrl() {
        return homePageUrl;
    }

    public void setHomePageUrl(String homePageUrl) {
        this.homePageUrl = homePageUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public List<JSONItem> getItems() {
        return items;
    }

    public void setItems(List<JSONItem> items) {
        this.items = items;
    }
}
