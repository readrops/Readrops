package com.readrops.api.utils

import junit.framework.TestCase.assertEquals
import org.junit.Test

class LibUtilsTest {

    @Test
    fun contentTypeWithCharsetTest() {
        assertEquals(LibUtils.parseContentType("application/rss+xml; charset=UTF-8"),
                "application/rss+xml")
    }

    @Test
    fun contentTypeWithoutCharsetText() {
        assertEquals(LibUtils.parseContentType("text/xml"),
                "text/xml")
    }

    @Test
    fun cleanTextTest() {
        val text = "    <p>This is a text<br/>to</p> clean    "
        assertEquals("This is a text to clean", LibUtils.cleanText(text))
    }
}