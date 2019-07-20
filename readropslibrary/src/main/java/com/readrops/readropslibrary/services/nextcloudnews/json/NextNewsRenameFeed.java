package com.readrops.readropslibrary.services.nextcloudnews.json;

public class NextNewsRenameFeed {

    private int id;

    private String feedTitle;

    public NextNewsRenameFeed(int id, String feedTitle) {
        this.id = id;
        this.feedTitle = feedTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }
}
