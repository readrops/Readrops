package com.readrops.api.localfeed

import com.readrops.api.localfeed.atom.ATOMFeedAdapter
import com.readrops.api.localfeed.atom.ATOMItemsAdapter
import com.readrops.api.localfeed.rss1.RSS1FeedAdapter
import com.readrops.api.localfeed.rss2.RSS2FeedAdapter
import com.readrops.api.localfeed.rss2.RSS2ItemsAdapter
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Test

class XmlAdapterTest {

    @Test
    fun xmlFeedAdapterFactoryTest() {
        assertTrue(XmlAdapter.xmlFeedAdapterFactory(LocalRSSHelper.RSSType.RSS_1) is RSS1FeedAdapter)
        assertTrue(XmlAdapter.xmlFeedAdapterFactory(LocalRSSHelper.RSSType.RSS_2) is RSS2FeedAdapter)
        assertTrue(XmlAdapter.xmlFeedAdapterFactory(LocalRSSHelper.RSSType.ATOM) is ATOMFeedAdapter)

        Assert.assertThrows(IllegalArgumentException::class.java) { XmlAdapter.xmlFeedAdapterFactory(LocalRSSHelper.RSSType.UNKNOWN) }
    }

    @Test
    fun xmlItemsAdapterFactoryTest() {
        assertTrue(XmlAdapter.xmlItemsAdapterFactory(LocalRSSHelper.RSSType.RSS_2) is RSS2ItemsAdapter)
        assertTrue(XmlAdapter.xmlItemsAdapterFactory(LocalRSSHelper.RSSType.ATOM) is ATOMItemsAdapter)

        Assert.assertThrows(IllegalArgumentException::class.java) { XmlAdapter.xmlItemsAdapterFactory(LocalRSSHelper.RSSType.UNKNOWN) }
    }
}