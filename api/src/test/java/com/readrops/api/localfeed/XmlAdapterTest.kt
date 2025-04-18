package com.readrops.api.localfeed

import com.readrops.api.localfeed.atom.ATOMFeedAdapter
import com.readrops.api.localfeed.rss1.RSS1FeedAdapter
import com.readrops.api.localfeed.rss2.RSS2FeedAdapter
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class XmlAdapterTest {

    @Test
    fun xmlFeedAdapterFactoryTest() {
        assertTrue(XmlAdapter.xmlFeedAdapterFactory(LocalRSSHelper.RSSType.RSS_1) is RSS1FeedAdapter)
        assertTrue(XmlAdapter.xmlFeedAdapterFactory(LocalRSSHelper.RSSType.RSS_2) is RSS2FeedAdapter)
        assertTrue(XmlAdapter.xmlFeedAdapterFactory(LocalRSSHelper.RSSType.ATOM) is ATOMFeedAdapter)

        assertThrows(java.lang.IllegalArgumentException::class.java) {
            XmlAdapter.xmlFeedAdapterFactory(LocalRSSHelper.RSSType.UNKNOWN)
        }
    }
}