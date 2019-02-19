package com.readrops.readropslibrary.localfeed.rss;

import com.readrops.readropslibrary.localfeed.AItem;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "item", strict = false)
public class RSSItem extends AItem {

    @Element(name = "title", required = false)
    private String title;

    @Element(name = "description", required = false, data = true)
    private String description;

    @Element(name = "link", required = false)
    private String link;

    @Element(name = "imageLink", required = false)
    private String imageLink;

    @ElementList(name = "content", inline = true, required = false)
    @Namespace(prefix = "media")
    private List<RSSMediaContent> mediaContents;

    @ElementList(name = "enclosure", inline = true, required = false)
    private List<RSSEnclosure> enclosures;

    @Element(name = "creator", required = false)
    @Namespace(prefix = "dc", reference = "http://purl.org/dc/elements/1.1/")
    private String author;

    @Element(name = "pubDate", required = false)
    private String pubDate;

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
}
