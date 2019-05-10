package com.readrops.readropslibrary.services.nextcloudnews.json;

public class NextNewsFeed {

    private int id;

    private String url;

    private String title;

    private String faviconLink;

    private float added;

    private float folderId;

    private float unreadCount;

    private float ordering;

    private String link;

    private boolean pinned;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFaviconLink() {
        return faviconLink;
    }

    public void setFaviconLink(String faviconLink) {
        this.faviconLink = faviconLink;
    }

    public float getAdded() {
        return added;
    }

    public void setAdded(float added) {
        this.added = added;
    }

    public float getFolderId() {
        return folderId;
    }

    public void setFolderId(float folderId) {
        this.folderId = folderId;
    }

    public float getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(float unreadCount) {
        this.unreadCount = unreadCount;
    }

    public float getOrdering() {
        return ordering;
    }

    public void setOrdering(float ordering) {
        this.ordering = ordering;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
