package com.readrops.readropslibrary;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public final class PageParser {

    public static String getFeedLink(String url) {
        String feedUrl = null;

        try {
            Document document = Jsoup.connect(url).get();

            Elements elements = document.select("link");

            for (Element element : elements) {
                String type = element.attributes().get("type");

                if (isTypeRssFeed(type)) {
                    feedUrl = element.attributes().get("href");
                    break;
                }
            }

            return feedUrl;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static boolean isTypeRssFeed(String type) {
        return type.equals("application/rss+xml") || type.equals("application/atom+xml") || type.equals("application/json");
    }

}
