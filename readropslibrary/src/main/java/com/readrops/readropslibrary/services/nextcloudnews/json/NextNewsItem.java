package com.readrops.readropslibrary.services.nextcloudnews.json;

public class NextNewsItem {

    private int id;

    private String guid;

    private String guidHash;

    private String url;

    private String title;

    private String author;

    private long pubDate;

    private String body;

    private String enclosureMime;

    private String enclosureLink;

    private int feedId;

    private boolean unread;

    private boolean starred;

    private long lastModified;

    private String fingerprint;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getGuidHash() {
        return guidHash;
    }

    public void setGuidHash(String guidHash) {
        this.guidHash = guidHash;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getPubDate() {
        return pubDate;
    }

    public void setPubDate(long pubDate) {
        this.pubDate = pubDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getEnclosureMime() {
        return enclosureMime;
    }

    public void setEnclosureMime(String enclosureMime) {
        this.enclosureMime = enclosureMime;
    }

    public String getEnclosureLink() {
        return enclosureLink;
    }

    public void setEnclosureLink(String enclosureLink) {
        this.enclosureLink = enclosureLink;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}
