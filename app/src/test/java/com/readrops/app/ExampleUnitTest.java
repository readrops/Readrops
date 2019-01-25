package com.readrops.app;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void stringToDateTest() {
        LocalDateTime dateTime = new LocalDateTime(2019, 1, 4, 22, 21, 46);

        try {
            // RSS
            assertTrue(dateTime.compareTo(DateUtils.stringToDateTime("Fri, 04 Jan 2019 22:21:46 +0000", DateUtils.RSS_DATE_FORMAT)) == 0);

            // ATOM
            assertTrue(dateTime.compareTo(DateUtils.stringToDateTime("2019-01-04T22:21:46+00:00", DateUtils.ATOM_JSON_DATE_FORMAT)) == 0);

            // JSON
            assertTrue(dateTime.compareTo(DateUtils.stringToDateTime("2019-01-04T22:21:46-0000", DateUtils.ATOM_JSON_DATE_FORMAT)) == 0);


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}