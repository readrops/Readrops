package com.readrops.api.localfeed.rss2

import com.readrops.api.TestUtils
import com.readrops.api.utils.exceptions.ParseException
import junit.framework.TestCase.assertEquals
import org.junit.Test

class RSS2FeedAdapterTest {


    private val adapter = RSS2FeedAdapter()

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_full_feed.xml")

        val feed = adapter.fromXml(stream)

        assertEquals(feed.name, "Hacker News")
        assertEquals(feed.url, "https://news.ycombinator.com/feed/")
        assertEquals(feed.siteUrl, "https://news.ycombinator.com/")
        assertEquals(feed.description, "Links for the intellectually curious, ranked by readers.")
    }


    @Test(expected = ParseException::class)
    fun nullTitleTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_feed_special_cases.xml")
        adapter.fromXml(stream)
    }
}