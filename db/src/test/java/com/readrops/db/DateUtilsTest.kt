package com.readrops.db

import com.readrops.db.util.DateUtils
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class DateUtilsTest {

    @Test
    fun rssDateTest() {
        val dateTime = LocalDateTime.of(2019, 1, 4, 22, 21, 46)
        assertEquals(0, dateTime.compareTo(DateUtils.parse("Fri, 04 Jan 2019 22:21:46 GMT")))
    }

    @Test
    fun rssDate2Test() {
        val dateTime = LocalDateTime.of(2019, 1, 4, 22, 21, 46)
        assertEquals(0, dateTime.compareTo(DateUtils.parse("Fri, 04 Jan 2019 22:21:46 +0000")))
    }

    @Test
    fun rssDate3Test() {
        val dateTime = LocalDateTime.of(2019, 1, 4, 22, 21, 46)
        assertEquals(0, dateTime.compareTo(DateUtils.parse("Fri, 04 Jan 2019 22:21:46")))
    }

    @Test
    fun edtPatternTest() {
        val dateTime = LocalDateTime.of(2020, 7, 17, 16, 30, 0)
        assertEquals(0, dateTime.compareTo(DateUtils.parse("Fri, 17 Jul 2020 16:30:00 EDT")))
    }

    @Test
    fun atomJsonDateTest() {
        val dateTime = LocalDateTime.of(2019, 1, 4, 22, 21, 46)
        assertEquals(0, dateTime.compareTo(DateUtils.parse("2019-01-04T22:21:46+00:00")))
    }

    @Test
    fun atomJsonDate2Test() {
        val dateTime = LocalDateTime.of(2019, 1, 4, 22, 21, 46)
        assertEquals(0, dateTime.compareTo(DateUtils.parse("2019-01-04T22:21:46-0000")))
    }

    @Test
    fun isoPatternTest() {
        val dateTime = LocalDateTime.of(2020, 6, 30, 11, 39, 37, 206000000)
        assertEquals(0, dateTime.compareTo(DateUtils.parse("2020-06-30T11:39:37.206-07:00")))
    }
}