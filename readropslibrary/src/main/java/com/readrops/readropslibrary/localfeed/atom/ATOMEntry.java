package com.readrops.readropslibrary.localfeed.atom;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "entry", strict = false)
public class ATOMEntry {

    @Element(required = false)
    private String title;

    @Element(required = false)
    private ATOMLink link;

    @Element(required = false)
    private String updated;

    @Element(required = false)
    private String summary;

    @Element(required = false)
    private String content;

    @Attribute(name = "type", required = false)
    private String contentType;


}
