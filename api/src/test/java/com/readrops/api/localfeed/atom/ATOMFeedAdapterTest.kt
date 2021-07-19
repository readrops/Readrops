package com.readrops.api.localfeed.atom

import com.readrops.api.TestUtils
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ATOMFeedAdapterTest {

    private val adapter = ATOMFeedAdapter()

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_feed.xml")

        val feed = adapter.fromXml(stream)

        assertEquals(feed.name, "Recent Commits to Readrops:develop")
        assertEquals(feed.url, "https://github.com/readrops/Readrops/commits/develop.atom")
        assertEquals(feed.siteUrl, "https://github.com/readrops/Readrops/commits/develop")
        assertEquals(feed.description, "Here is a subtitle")
    }
}