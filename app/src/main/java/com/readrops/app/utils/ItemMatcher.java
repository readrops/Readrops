package com.readrops.app.utils;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItem;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

public final class ItemMatcher {

    public static Item nextNewsItemToItem(NextNewsItem nextNewsItem, Feed feed) {
        Item item = new Item();

        item.setRemoteId(nextNewsItem.getId());
        item.setTitle(nextNewsItem.getTitle());

        if (!nextNewsItem.getAuthor().isEmpty())
            item.setAuthor(nextNewsItem.getAuthor());

        item.setPubDate(new LocalDateTime(nextNewsItem.getPubDate() * 1000L,
                DateTimeZone.getDefault()));
        item.setContent(nextNewsItem.getBody());

        if (Utils.isTypeImage(nextNewsItem.getEnclosureMime()))
            item.setImageLink(nextNewsItem.getEnclosureLink());

        item.setLink(nextNewsItem.getUrl());
        item.setGuid(nextNewsItem.getGuid());
        item.setRead(!nextNewsItem.isUnread());

        item.setFeedId(feed.getId());

        return item;
    }
}
