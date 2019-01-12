package com.readrops.readropslibrary.localfeed.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "channel", strict = false)
public class RSSChannel {

    @Element(name = "title", required = false)
    private String title;

    @Element(name = "description", required = false)
    private String description;

    @Element(name = "link", required = false)
    private String link;

    @Element(required = false)
    private RSSImage image;

    @ElementList(inline = true, required = false)
    private List<RSSItem> items;


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

    public List<RSSItem> getItems() {
        return items;
    }

    public void setItems(List<RSSItem> items) {
        this.items = items;
    }
}
