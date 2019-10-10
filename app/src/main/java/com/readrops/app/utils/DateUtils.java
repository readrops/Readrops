package com.readrops.app.utils;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.Locale;

public final class DateUtils {

    /**
     * Base of common RSS 2 date formats.
     * Examples :
     * Fri, 04 Jan 2019 22:21:46 GMT
     * Fri, 04 Jan 2019 22:21:46 +0000
     */
    private static final String RSS_2_BASE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss ";

    private static final String GMT_PATTERN = "ZZZ";

    private static final String OFFSET_PATTERN = "Z";

    /**
     * Date pattern for format : 2019-01-04T22:21:46+00:00
     */
    private static final String ATOM_JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static LocalDateTime stringToLocalDateTime(String value) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormat.forPattern(RSS_2_BASE_PATTERN).getParser())
                .appendOptional(DateTimeFormat.forPattern(ATOM_JSON_DATE_FORMAT).getParser())
                .appendOptional(DateTimeFormat.forPattern(GMT_PATTERN).getParser())
                .appendOptional(DateTimeFormat.forPattern(OFFSET_PATTERN).getParser())
                .toFormatter()
                .withLocale(Locale.ENGLISH)
                .withOffsetParsed();

        return formatter.parseLocalDateTime(value);
    }

    public static String formattedDateByLocal(LocalDateTime dateTime) {
        return DateTimeFormat.mediumDate()
                .withLocale(Locale.getDefault())
                .print(dateTime);
    }

    public static String formattedDateTimeByLocal(LocalDateTime dateTime) {
        return DateTimeFormat.forPattern("dd MMM yyyy · HH:mm")
                .withLocale(Locale.getDefault())
                .print(dateTime);
    }
}
