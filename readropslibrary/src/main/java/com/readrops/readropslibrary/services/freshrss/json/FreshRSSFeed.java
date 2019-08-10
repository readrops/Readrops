
package com.readrops.readropslibrary.services.freshrss.json;

import java.util.List;

public class FreshRSSFeed {

    private List<FreshRSSCategory> categories;

    private String htmlUrl;

    private String iconUrl;

    private String id;

    private String title;

    private String url;

    public List<FreshRSSCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<FreshRSSCategory> categories) {
        this.categories = categories;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
