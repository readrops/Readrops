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
}