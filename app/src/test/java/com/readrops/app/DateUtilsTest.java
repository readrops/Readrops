package com.readrops.app;

import com.readrops.app.utils.DateUtils;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateUtilsTest {

    @Test
    public void rssDateTest() {
        LocalDateTime dateTime = new LocalDateTime(2019, 1, 4, 22, 21, 46);

        assertEquals(0, dateTime.compareTo(DateUtils.stringToLocalDateTime("Fri, 04 Jan 2019 22:21:46 GMT")));
    }

    @Test
    public void rssDate2Test() {
        LocalDateTime dateTime = new LocalDateTime(2019, 1, 4, 22, 21, 46);

        assertEquals(0, dateTime.compareTo(DateUtils.stringToLocalDateTime("Fri, 04 Jan 2019 22:21:46 +0000")));
    }

    @Test
    public void atomJsonDateTest() {
        LocalDateTime dateTime = new LocalDateTime(2019, 1, 4, 22, 21, 46);

        assertEquals(0, dateTime.compareTo(DateUtils.stringToLocalDateTime("2019-01-04T22:21:46+00:00")));
    }

    @Test
    public void atomJsonDate2Test() {
        LocalDateTime dateTime = new LocalDateTime(2019, 1, 4, 22, 21, 46);

        assertEquals(0, dateTime.compareTo(DateUtils.stringToLocalDateTime("2019-01-04T22:21:46-0000")));
    }

    @Test
    public void isoPatternTest() {
        LocalDateTime dateTime = new LocalDateTime(2020, 6, 30, 11, 39, 37, 206);

        assertEquals(0, dateTime.compareTo(DateUtils.stringToLocalDateTime("2020-06-30T11:39:37.206-07:00")));
    }

    @Test
    public void edtPatternTest() {
        LocalDateTime dateTime = new LocalDateTime(2020, 7, 17, 16, 30, 0);

        assertEquals(0, dateTime.compareTo(DateUtils.stringToLocalDateTime("Fri, 17 Jul 2020 16:30:00 EDT")));
    }
}