package com.readrops.app.utils;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.localfeed.atom.ATOMEntry;
import com.readrops.readropslibrary.localfeed.json.JSONItem;
import com.readrops.readropslibrary.localfeed.rss.RSSEnclosure;
import com.readrops.readropslibrary.localfeed.rss.RSSItem;
import com.readrops.readropslibrary.localfeed.rss.RSSMediaContent;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItem;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItem;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.jsoup.Jsoup;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

    public static List<Item> itemsFromRSS(List<RSSItem> items, Feed feed) throws ParseException {
        List<Item> dbItems = new ArrayList<>();

        for(RSSItem item : items) {
            Item newItem = new Item();

            newItem.setAuthor(item.getCreator());
            newItem.setContent(item.getContent());
            newItem.setDescription(item.getDescription());
            newItem.setGuid(item.getGuid());
            newItem.setTitle(Jsoup.parse(item.getTitle()).text());

            // I wish I hadn't done that...
            if (Pattern.compile(DateUtils.RSS_ALTERNATIVE_DATE_FORMAT_REGEX).matcher(item.getDate()).matches())
                newItem.setPubDate(DateUtils.stringToDateTime(item.getDate(), DateUtils.RSS_2_DATE_FORMAT_3));
            else {
                try {
                    newItem.setPubDate(DateUtils.stringToDateTime(item.getDate(), DateUtils.RSS_2_DATE_FORMAT_2));
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    if (newItem.getPubDate() == null) {
                        try {
                            newItem.setPubDate(DateUtils.stringToDateTime(item.getDate(), DateUtils.RSS_2_DATE_FORMAT));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } finally {
                            newItem.setPubDate(DateUtils.stringToDateTime(item.getDate(), DateUtils.ATOM_JSON_DATE_FORMAT));
                        }
                    }
                }
            }

            newItem.setLink(item.getLink());
            newItem.setFeedId(feed.getId());

            if (item.getMediaContents() != null && item.getMediaContents().size() > 0) {
                for (RSSMediaContent mediaContent : item.getMediaContents()) {
                    if (mediaContent.getMedium() != null && Utils.isTypeImage(mediaContent.getMedium())) {
                        newItem.setImageLink(mediaContent.getUrl());
                        break;
                    }
                }
            } else {
                if (item.getEnclosures() != null) {
                    for (RSSEnclosure enclosure : item.getEnclosures()) {
                        if (enclosure.getType() != null && Utils.isTypeImage(enclosure.getType())) {
                            newItem.setImageLink(enclosure.getUrl());
                            break;
                        }
                    }

                }
            }

            dbItems.add(newItem);
        }

        return dbItems;
    }

    public static List<Item> itemsFromATOM(List<ATOMEntry> items, Feed feed) throws ParseException {
        List<Item> dbItems = new ArrayList<>();

        for (ATOMEntry item : items) {
            Item dbItem = new Item();

            dbItem.setContent(item.getContent());
            dbItem.setDescription(item.getSummary());
            dbItem.setGuid(item.getId());
            dbItem.setTitle(item.getTitle());

            dbItem.setPubDate(DateUtils.stringToDateTime(item.getUpdated(), DateUtils.ATOM_JSON_DATE_FORMAT));
            dbItem.setLink(item.getUrl());

            dbItem.setFeedId(feed.getId());

            dbItems.add(dbItem);
        }

        return dbItems;
    }

    public static List<Item> itemsFromJSON(List<JSONItem> items, Feed feed) throws ParseException {
        List<Item> dbItems = new ArrayList<>();

        for (JSONItem item : items) {
            Item dbItem = new Item();

            if (item.getAuthor() != null)
                dbItem.setAuthor(item.getAuthor().getName());

            dbItem.setContent(item.getContent());
            dbItem.setDescription(item.getSummary());
            dbItem.setGuid(item.getId());
            dbItem.setTitle(item.getTitle());

            dbItem.setPubDate(DateUtils.stringToDateTime(item.getPubDate(), DateUtils.ATOM_JSON_DATE_FORMAT));

            dbItem.setLink(item.getUrl());

            dbItem.setFeedId(feed.getId());

            dbItems.add(dbItem);
        }

        return dbItems;
    }
}
