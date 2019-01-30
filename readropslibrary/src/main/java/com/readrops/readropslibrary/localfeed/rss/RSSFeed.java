package com.readrops.readropslibrary.localfeed.rss;

import com.readrops.readropslibrary.localfeed.AFeed;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "rss", strict = false)
public class RSSFeed extends AFeed {

    @Element(name = "channel", required = false)
    private RSSChannel channel;

    public RSSChannel getChannel() {
        return channel;
    }

    public void setChannel(RSSChannel channel) {
        this.channel = channel;
    }
}
