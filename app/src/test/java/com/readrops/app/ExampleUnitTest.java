package com.readrops.app;

import com.readrops.app.utils.DateUtils;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void rssDateTest() {
        try {
            LocalDateTime dateTime = new LocalDateTime(2019, 1, 4, 22, 21, 46);

            assertEquals(0, dateTime.compareTo(DateUtils.stringToDateTime("Fri, 04 Jan 2019 22:21:46 +0000", DateUtils.RSS_2_DATE_FORMAT)));
            assertEquals(0, dateTime.compareTo(DateUtils.stringToDateTime("Fri, 04 Jan 2019 22:21:46 GMT", DateUtils.RSS_2_DATE_FORMAT_2)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void atomJsonDateTest() {
        try {
            LocalDateTime dateTime = new LocalDateTime(2019, 1, 4, 22, 21, 46);

            assertEquals(0, dateTime.compareTo(DateUtils.stringToDateTime("2019-01-04T22:21:46+00:00", DateUtils.ATOM_JSON_DATE_FORMAT)));
            assertEquals(0, dateTime.compareTo(DateUtils.stringToDateTime("2019-01-04T22:21:46-0000", DateUtils.ATOM_JSON_DATE_FORMAT)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void timeStamptoDateTest() {
        LocalDateTime localDateTime = new LocalDateTime(1367270544 * 1000L, DateTimeZone.getDefault());

        assertEquals(0, localDateTime.compareTo(new LocalDateTime(2013, 4, 29, 21, 22, 24)));
    }
}