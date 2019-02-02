package com.readrops.readropslibrary.Utils;

import java.io.InputStream;
import java.util.Scanner;

public final class Utils {

    public static final String RSS_CONTENT_TYPE = "application/rss+xml";
    public static final String RSS_TEXT_CONTENT_TYPE = "text/xml";
    public static final String ATOM_CONTENT_TYPE = "application/atom+xml";
    public static final String JSON_CONTENT_TYPE = "application/json";

    public static String inputStreamToString(InputStream input) {
        Scanner scanner = new Scanner(input).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

}