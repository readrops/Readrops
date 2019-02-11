package com.readrops.readropslibrary.localfeed.rss;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "content", strict = false)
public class RSSMediaContent {

    @Attribute(required = false)
    private String url;

    @Attribute(required = false)
    private String medium;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }
}
