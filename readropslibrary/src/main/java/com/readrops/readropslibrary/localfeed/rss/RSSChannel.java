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

    // workaround to get the two links (feed and regular)
    @ElementList(name = "link", inline = true, required = false)
    private List<RSSLink> links;

    @Element(name = "lastBuildDate", required = false)
    private String lastUpdated;

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

    public List<RSSLink> getLinks() {
        return links;
    }

    public void setLinks(List<RSSLink> links) {
        this.links = links;
    }

    public List<RSSItem> getItems() {
        return items;
    }

    public void setItems(List<RSSItem> items) {
        this.items = items;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public RSSImage getImage() {
        return image;
    }

    public void setImage(RSSImage image) {
        this.image = image;
    }

    public String getFeedUrl() {
        if (links.size() > 0)
            return links.get(0).getHref();
        else
            return null;
    }

    public String getUrl() {
        if (links.size() > 1)
            return links.get(1).getText();
        else
            return null;
    }
}
