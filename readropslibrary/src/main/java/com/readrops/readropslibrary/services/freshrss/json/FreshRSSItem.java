
package com.readrops.readropslibrary.services.freshrss.json;

import java.util.List;

public class FreshRSSItem {

    private List<FreshRSSAlternate> alternate;

    private String author;

    private List<String> categories;

    private String crawlTimeMsec;

    private String id;

    private FreshRSSOrigin origin;

    private Long published;

    private FreshRSSSummary summary;

    private String timestampUsec;

    private String title;

    public List<FreshRSSAlternate> getAlternate() {
        return alternate;
    }

    public void setAlternate(List<FreshRSSAlternate> alternate) {
        this.alternate = alternate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getCrawlTimeMsec() {
        return crawlTimeMsec;
    }

    public void setCrawlTimeMsec(String crawlTimeMsec) {
        this.crawlTimeMsec = crawlTimeMsec;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FreshRSSOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(FreshRSSOrigin origin) {
        this.origin = origin;
    }

    public Long getPublished() {
        return published;
    }

    public void setPublished(Long published) {
        this.published = published;
    }

    public FreshRSSSummary getSummary() {
        return summary;
    }

    public void setSummary(FreshRSSSummary summary) {
        this.summary = summary;
    }

    public String getTimestampUsec() {
        return timestampUsec;
    }

    public void setTimestampUsec(String timestampUsec) {
        this.timestampUsec = timestampUsec;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
