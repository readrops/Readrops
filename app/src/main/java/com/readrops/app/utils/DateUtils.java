package com.readrops.app.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.joda.time.LocalDateTime;

public final class DateUtils {

    public static final String RSS_1_DATE_FORMAT_REGEX = "^[a-zA-Z]{3}, [0-9]{2} [a-zA-Z]{3} [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2} Z$";

    public static final String RSS_2_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";
    public static final String RSS_1_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'Z'";

    public static final String ATOM_JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";

    public static LocalDateTime stringToDateTime(String value, String pattern) throws ParseException {
        DateFormat formatter = new SimpleDateFormat(pattern, Locale.ENGLISH);
        return new LocalDateTime(formatter.parse(value));
    }

    public static String formatedDateByLocal(LocalDateTime dateTime) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

        return df.format(dateTime.toDate());
    }
}
