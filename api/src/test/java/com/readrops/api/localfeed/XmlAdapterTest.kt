package com.readrops.api.localfeed

import com.readrops.api.localfeed.atom.ATOMFeedAdapter
import com.readrops.api.localfeed.rss1.RSS1FeedAdapter
import com.readrops.api.localfeed.rss2.RSS2FeedAdapter
import junit.framework.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class XmlAdapterTest {

    @get:Rule
    val expectedException: ExpectedException = ExpectedException.none()

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