package com.readrops.api.localfeed.rss1

import com.readrops.api.TestUtils
import junit.framework.Assert.assertEquals
import org.junit.Test

class RSS1FeedAdapterTest {

    private val adapter = RSS1FeedAdapter()

    @Test
    fun normalCaseTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_feed.xml")

        val feed = adapter.fromXml(stream)

        assertEquals(feed.name, "Slashdot")
        assertEquals(feed.url, "https://slashdot.org/")
        assertEquals(feed.siteUrl, "https://slashdot.org/")
        assertEquals(feed.description, "News for nerds, stuff that matters")
    }
}