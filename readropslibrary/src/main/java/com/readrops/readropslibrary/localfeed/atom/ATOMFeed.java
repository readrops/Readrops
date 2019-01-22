package com.readrops.readropslibrary.localfeed.atom;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "feed", strict = false)
public class ATOMFeed {

    @Element(required = false)
    private String title;

    @Element(name = "href", required = false)
    private String link;

    @Element(required = false)
    private String subtitle;

    @Element(required = false)
    private String updated;

    @Element(required = false)
    private ATOMAuthor author;

    @ElementList(inline = true, required = false)
    private List<ATOMEntry> entries;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public ATOMAuthor getAuthor() {
        return author;
    }

    public void setAuthor(ATOMAuthor author) {
        this.author = author;
    }

    public List<ATOMEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ATOMEntry> entries) {
        this.entries = entries;
    }
}
