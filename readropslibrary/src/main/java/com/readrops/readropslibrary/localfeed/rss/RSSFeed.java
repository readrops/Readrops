package com.readrops.readropslibrary.localfeed.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "rss", strict = false)
public class RSSFeed {

    @Element(name = "channel", required = false)
    private RSSChannel channel;

    public RSSChannel getChannel() {
        return channel;
    }

    public void setChannel(RSSChannel channel) {
        this.channel = channel;
    }
}
