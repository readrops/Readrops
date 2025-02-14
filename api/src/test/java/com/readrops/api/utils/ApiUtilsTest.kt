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

    @Test
    fun handleRssSpecialCases() {
        assertEquals("https://example.com", ApiUtils.handleRssSpecialCases("https://example.com"))
        assertEquals(
            "https://www.youtube.com/@user",
            ApiUtils.handleRssSpecialCases("https://www.youtube.com/@user")
        )
        val playlistId = "qog2gifixwn3vitjneusb9xl"
        assertEquals(
            "https://www.youtube.com/feeds/videos.xml?playlist_id=$playlistId",
            ApiUtils.handleRssSpecialCases("https://www.youtube.com/watch?v=qjshdbmlk&list=$playlistId")
        )
        assertEquals(
            "https://www.youtube.com/feeds/videos.xml?playlist_id=$playlistId",
            ApiUtils.handleRssSpecialCases("https://youtu.be/watch?v=qjshdbmlk&list=$playlistId")
        )
    }
}