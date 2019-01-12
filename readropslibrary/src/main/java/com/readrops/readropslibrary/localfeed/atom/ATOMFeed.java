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

}
