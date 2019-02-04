package com.readrops.readropslibrary.localfeed.atom;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "link", strict = false)
public class ATOMLink {

    @Attribute(name = "href", required = false)
    private String href;

    @Attribute(name = "rel", required = false)
    private String rel;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }
}
