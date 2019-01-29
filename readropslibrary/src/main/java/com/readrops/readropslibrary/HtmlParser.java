package com.readrops.readropslibrary;

import android.text.LoginFilter;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class HtmlParser {

    /**
     * Parse the html page to get all rss urls
     * @param url url to request
     * @return a list of rss urls with their title
     */
    public static List<ParsingResult> getFeedLink(String url) {
        List<ParsingResult> results = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();

            Elements elements = document.select("link");

            for (Element element : elements) {
                String type = element.attributes().get("type");

                if (isTypeRssFeed(type)) {
                    String feedUrl = element.attributes().get("href");
                    String label = element.attributes().get("title");

                    results.add(new ParsingResult(feedUrl, label));
                }
            }

            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static boolean isTypeRssFeed(String type) {
        return type.equals("application/rss+xml") || type.equals("application/atom+xml") || type.equals("application/json");
    }

    /**
     * get the feed item image based on open graph metadata.
     * Warning, This method is slow.
     * @param url url to request
     * @return the item image
     */
    public static String getOGImageLink(String url) {
        String imageUrl = null;

        try {

            long start = System.currentTimeMillis();
            Connection.Response response = Jsoup.connect(url).execute();

            String body = response.body();
            String head = body.substring(body.indexOf("<head>"), body.indexOf("</head>"));

            Document document = Jsoup.parse(head);
            Element element = document.select("meta[property=og:image]").first();

            if (element != null)
                imageUrl = element.attributes().get("content");

            long end = System.currentTimeMillis();

            Log.d("temps de parsing", String.valueOf(end - start));

            return imageUrl;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getDescImageLink(String description) {
        Document document = Jsoup.parse(description);

        return document.select("img").first().attr("src");
    }
}
