package com.readrops.readropslibrary.localfeed.atom;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "href", strict = false)
public class ATOMLink {

    @Attribute(name = "href", required = false)
    private String href;

    public String getHref() {
        return href;
    }
}
