package com.readrops.app.database.entities;

import android.arch.persistence.room.*;

import com.readrops.app.utils.DateUtils;
import com.readrops.app.utils.Utils;
import com.readrops.readropslibrary.localfeed.atom.ATOMEntry;
import com.readrops.readropslibrary.localfeed.json.JSONItem;
import com.readrops.readropslibrary.localfeed.rss.RSSEnclosure;
import com.readrops.readropslibrary.localfeed.rss.RSSItem;
import com.readrops.readropslibrary.localfeed.rss.RSSMediaContent;

import org.joda.time.LocalDateTime;
import org.jsoup.Jsoup;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity
(foreignKeys = @ForeignKey(entity = Feed.class, parentColumns = "id", childColumns = "feed_id", onDelete = CASCADE))
public class Item {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;

    private String description;

    @ColumnInfo(name = "clean_description")
    private String cleanDescription;

    private String link;

    @ColumnInfo(name = "image_link")
    private String imageLink;

    private String author;

    @ColumnInfo(name = "pub_date")
    private LocalDateTime pubDate;

    private String content;

    @ColumnInfo(name = "feed_id")
    private int feedId;

    @ColumnInfo(index = true)
    private String guid;

    @ColumnInfo(name = "read_time")
    private double readTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCleanDescription() {
        return cleanDescription;
    }

    public void setCleanDescription(String cleanDescription) {
        this.cleanDescription = cleanDescription;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getPubDate() {
        return pubDate;
    }

    public void setPubDate(LocalDateTime pubDate) {
        this.pubDate = pubDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public boolean hasImage() {
        return getImageLink() != null;
    }

    public double getReadTime() {
        return readTime;
    }

    public void setReadTime(double readTime) {
        this.readTime = readTime;
    }

    public String getText() {
        if (content != null)
            return content;
        else
            return description;
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
