package com.readrops.readropslibrary.localfeed.json;

import com.google.gson.annotations.SerializedName;

public class JSONItem {

    private String id;

    private String title;

    private String summary;

    @SerializedName("content_text")
    private String contentText;

    @SerializedName("content_html")
    private String contentHtml;

    private String url;

    @SerializedName("image")
    private String imageUrl;

    @SerializedName("date_published")
    private String pubDate;

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getModDate() {
        return modDate;
    }

    public void setModDate(String modDate) {
        this.modDate = modDate;
    }

    public JSONAuthor getAuthor() {
        return author;
    }

    public void setAuthor(JSONAuthor author) {
        this.author = author;
    }

    @SerializedName("date_modified")
    private String modDate;

    private JSONAuthor author;
}
