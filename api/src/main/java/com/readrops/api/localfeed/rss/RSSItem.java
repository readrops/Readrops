package com.readrops.api.localfeed.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "item", strict = false)
public class RSSItem {

    @Element
    private String title;

    @Element(name = "link", required = false)
    private String link;
    
    @Element(name = "imageLink", required = false)
    private String imageLink;

    @ElementList(name = "content", inline = true, required = false)
    @Namespace(prefix = "media")
    private List<RSSMediaContent> mediaContents;

    @ElementList(name = "enclosure", inline = true, required = false)
    private List<RSSEnclosure> enclosures;

    @ElementList(name = "creator", inline = true, required = false)
    @Namespace(prefix = "dc", reference = "http://purl.org/dc/elements/1.1/")
    private List<String> creator;

    @Element(required = false)
    private String author;

    @Element(name = "pubDate", required = false)
    private String pubDate;

    @Element(name = "date", required = false)
    @Namespace(prefix = "dc")
    private String date;

    @Element(name = "description", required = false)
    private String description;

    @Element(name = "encoded", required = false)
    @Namespace(prefix = "content")
    private String content;

    @Element(required = false)
    private String guid;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public List<String> getCreator() {
        return creator;
    }

    public void setCreator(List<String> creator) {
        this.creator = creator;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public List<RSSMediaContent> getMediaContents() {
        return mediaContents;
    }

    public void setMediaContents(List<RSSMediaContent> mediaContents) {
        this.mediaContents = mediaContents;
    }

    public List<RSSEnclosure> getEnclosures() {
        return enclosures;
    }

    public void setEnclosures(List<RSSEnclosure> enclosures) {
        this.enclosures = enclosures;
    }

    public String getDate() {
        if (pubDate != null)
            return pubDate;
        else
            return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        if (creator != null && !creator.isEmpty())
            return creator.get(0);
        else
            return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
