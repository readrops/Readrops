package com.readrops.readropslibrary.localfeed.atom;

import com.readrops.readropslibrary.localfeed.AItem;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "entry", strict = false)
public class ATOMEntry extends AItem  {

    @Element(required = false)
    private String title;

    @ElementList(name = "link", inline = true, required = false)
    private List<ATOMLink> links;

    @Element(required = false)
    private String updated;

    @Element(required = false)
    private String summary;

    @Element(required = false)
    private String id;

    @Element(required = false)
    private String content;

    @Attribute(name = "type", required = false)
    private String contentType;

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

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUrl() {
        if (links.size() > 0) {
            if (links.get(0).getRel() == null)
                return links.get(0).getHref();
            else {
                if (links.size() > 1) {
                    if (links.get(1).getRel() == null)
                        return links.get(1).getHref();
                    else
                        return null;
                } else
                    return null;
            }
        } else
            return null;
    }
}
