package com.readrops.api.utils

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ApiUtilsTest {

    @Test
    fun contentTypeWithCharsetTest() {
        assertEquals(ApiUtils.parseContentType("application/rss+xml; charset=UTF-8"),
                "application/rss+xml")
    }

    @Test
    fun contentTypeWithoutCharsetText() {
        assertEquals(ApiUtils.parseContentType("text/xml"),
                "text/xml")
    }

    @Test
    fun cleanTextTest() {
        val text = "    <p>This is a text<br/>to</p> clean    "
        assertEquals("This is a text to clean", ApiUtils.cleanText(text))
    }

    @Test
    fun md5hashTest() {
        val value = ApiUtils.md5hash("test")

        assertEquals(value, "98f6bcd4621d373cade4e832627b4f6")
    }
}