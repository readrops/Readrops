package com.readrops.api.localfeed.atom;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "author", strict = false)
public class ATOMAuthor {

    @Element(required = false)
    private String name;

    @Element(required = false)
    private String email;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
