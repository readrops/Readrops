package com.readrops.readropslibrary.localfeed.atom;

import com.readrops.readropslibrary.localfeed.AFeed;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "feed", strict = false)
public class ATOMFeed extends AFeed {

    @Element(required = false)
    private String title;

    @ElementList(name = "link", inline = true, required = false)
    private List<ATOMLink> links;

    @Element(required = false)
    private String id;

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

    public List<ATOMLink> getLinks() {
        return links;
    }

    public void setLinks(List<ATOMLink> links) {
        this.links = links;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWebSiteUrl() {
        return id;
    }

    public String getUrl() {
        if (links.size() > 0) {
            if (links.get(0).getRel() != null)
                return links.get(0).getHref();
            else {
                if (links.size() > 1)
                    return links.get(1).getHref();
                else
                    return null;
            }
        } else
            return null;
    }
}
