package com.readrops.app.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.api.utils.HttpManager;
import com.readrops.api.utils.LibUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

public final class HtmlParser {

    private static final String TAG = HtmlParser.class.getSimpleName();

    /**
     * Parse the html page to get all rss urls
     *
     * @param url url to request
     * @return a list of rss urls with their title
     */
    public static List<ParsingResult> getFeedLink(String url) {
        List<ParsingResult> results = new ArrayList<>();

        String head = getHTMLHeadFromUrl(url);
        if (head != null) {
            Document document = Jsoup.parse(head, url);

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
        } else {
            return Collections.emptyList();
        }
    }

    private static boolean isTypeRssFeed(String type) {
        return type.equals(LibUtils.RSS_DEFAULT_CONTENT_TYPE) ||
                type.equals(LibUtils.ATOM_CONTENT_TYPE) ||
                type.equals(LibUtils.JSON_CONTENT_TYPE) ||
                type.equals(LibUtils.RSS_TEXT_CONTENT_TYPE) ||
                type.equals(LibUtils.RSS_APPLICATION_CONTENT_TYPE);
    }

    @Nullable
    public static String getFaviconLink(@NonNull String url) {
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
    private static String getHTMLHeadFromUrl(@NonNull String url) {
        long start = System.currentTimeMillis();

        try {
            Response response = HttpManager.getInstance().getOkHttpClient()
                    .newCall(new Request.Builder().url(url).build()).execute();

            if (response.header("Content-Type").contains(LibUtils.HTML_CONTENT_TYPE)) {
                String body = response.body().string();
                String head = body.substring(body.indexOf("<head"), body.indexOf("</head>"));

                long end = System.currentTimeMillis();
                Log.d(TAG, "parsing time : " + (end - start));

                return head;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }
}
