package com.readrops.app.utils;

import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItem;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItem;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

public final class ItemMatcher {

    public static Item nextNewsItemToItem(NextNewsItem nextNewsItem, int feedId) {
        Item item = new Item();

        item.setRemoteId(String.valueOf(nextNewsItem.getId()));
        item.setTitle(nextNewsItem.getTitle());

        if (!nextNewsItem.getAuthor().isEmpty())
            item.setAuthor(nextNewsItem.getAuthor());

        item.setPubDate(new LocalDateTime(nextNewsItem.getPubDate() * 1000L,
                DateTimeZone.getDefault()));
        item.setContent(nextNewsItem.getBody());

        if (nextNewsItem.getEnclosureMime() != null && Utils.isTypeImage(nextNewsItem.getEnclosureMime()))
            item.setImageLink(nextNewsItem.getEnclosureLink());

        item.setLink(nextNewsItem.getUrl());
        item.setGuid(nextNewsItem.getGuid());
        item.setRead(!nextNewsItem.isUnread());

        item.setFeedId(feedId);

        return item;
    }

    public static Item freshRSSItemtoItem(FreshRSSItem item, int feedId) {
        Item newItem = new Item();

        newItem.setTitle(item.getTitle());
        newItem.setAuthor(item.getAuthor());

        newItem.setPubDate(new LocalDateTime(item.getPublished() * 1000L,
                DateTimeZone.getDefault()));
        newItem.setContent(item.getSummary().getContent());

        newItem.setLink(item.getAlternate().get(0).getHref());
        newItem.setFeedId(feedId);
        newItem.setRemoteId(item.getId());

        return newItem;
    }
}
