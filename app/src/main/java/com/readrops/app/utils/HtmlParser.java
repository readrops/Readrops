package com.readrops.app.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.readropslibrary.utils.LibUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class HtmlParser {

    private static final String TAG = HtmlParser.class.getSimpleName();

    public static final String COVER_IMAGE_REGEX = "^(<p>|(<div.*>))?<img.*>";

    /**
     * Parse the html page to get all rss urls
     *
     * @param url url to request
     * @return a list of rss urls with their title
     */
    public static List<ParsingResult> getFeedLink(String url) throws Exception {
        List<ParsingResult> results = new ArrayList<>();

        Document document = Jsoup.parse(getHTMLHeadFromUrl(url), url);

        Elements elements = document.select("link");

        for (Element element : elements) {
            String type = element.attributes().get("type");

            if (isTypeRssFeed(type)) {
                String feedUrl = element.absUrl("href");
                String label = element.attributes().get("title");

                results.add(new ParsingResult(feedUrl, label));
            }
        }

        return results;
    }

    private static boolean isTypeRssFeed(String type) {
        return type.equals(LibUtils.RSS_DEFAULT_CONTENT_TYPE) ||
                type.equals(LibUtils.ATOM_CONTENT_TYPE) ||
                type.equals(LibUtils.JSON_CONTENT_TYPE) ||
                type.equals(LibUtils.RSS_TEXT_CONTENT_TYPE) ||
                type.equals(LibUtils.RSS_APPLICATION_CONTENT_TYPE);
    }

    /**
     * get the feed item image based on open graph metadata.
     * Warning, This method is slow.
     *
     * @param url url to request
     * @return the item image
     */
    public static String getOGImageLink(String url) throws IOException {
        String imageUrl = null;

        String head = getHTMLHeadFromUrl(url);

        Document document = Jsoup.parse(head);
        Element element = document.select("meta[property=og:image]").first();

        if (element != null)
            imageUrl = element.attributes().get("content");

        return imageUrl;
    }

    @Nullable
    public static String getFaviconLink(@NonNull String url) throws IOException {
        String favUrl = null;

        String head = getHTMLHeadFromUrl(url);
        if (head == null)
            return null;

        Document document = Jsoup.parse(head, url);
        Elements elements = document.select("link");

        for (Element element : elements) {
            if (element.attributes().get("rel").toLowerCase().contains("icon")) {
                favUrl = element.absUrl("href");
                break;
            }
        }

        return favUrl;
    }

    @Nullable
    private static String getHTMLHeadFromUrl(@NonNull String url) throws IOException {
        long start = System.currentTimeMillis();
        Connection.Response response = Jsoup.connect(url).execute();

        if (response.contentType().contains(LibUtils.HTML_CONTENT_TYPE)) {
            String body = response.body();
            String head = body.substring(body.indexOf("<head"), body.indexOf("</head>"));

            long end = System.currentTimeMillis();
            Log.d(TAG, "parsing time : " + (end - start));

            return head;
        } else
            return null;
    }

    public static String getDescImageLink(String description, String url) {
        Document document = Jsoup.parse(description, url);
        Elements elements = document.select("img");

        if (!elements.isEmpty())
            return elements.first().absUrl("src");
        else
            return null;
    }

    public static String deleteCoverImage(String content) {
        Document document = Jsoup.parse(content);

        if (Pattern.compile(COVER_IMAGE_REGEX).matcher(document.body().html()).find()) {
            Elements elements = document.select("img");

            if (!elements.isEmpty())
                elements.first().remove();

            return document.toString();
        } else
            return content;
    }
}
