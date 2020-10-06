package com.readrops.api.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LibUtils {

    public static final String HTML_CONTENT_TYPE = "text/html";

    public static final String CONTENT_TYPE_HEADER = "content-type";
    public static final String ETAG_HEADER = "ETag";
    public static final String IF_NONE_MATCH_HEADER = "If-None-Match";
    public static final String LAST_MODIFIED_HEADER = "Last-Modified";
    public static final String IF_MODIFIED_HEADER = "If-Modified-Since";

    public static final int HTTP_UNPROCESSABLE = 422;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_CONFLICT = 409;

    private static final String RSS_CONTENT_TYPE_REGEX = "([^;]+)";

    public static boolean isMimeImage(@NonNull String type) {
        return type.equals("image") || type.equals("image/jpeg") || type.equals("image/jpg")
                || type.equals("image/png");
    }

    @Nullable
    public static String parseContentType(String header) {
        Matcher matcher = Pattern.compile(RSS_CONTENT_TYPE_REGEX)
                .matcher(header);

        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return null;
        }
    }

    /**
     * Remove html tags and trim the text
     *
     * @param text string to clean
     * @return cleaned text
     */
    public static String cleanText(String text) {
        return Jsoup.parse(text).text().trim();
    }
}
