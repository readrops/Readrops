package com.readrops.api.localfeed.rss2

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.TestUtils
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.exceptions.ParseException
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class RSS2AdapterTest {

    private val adapter = RSS2FeedAdapter()

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_feed.xml")

        val pair = adapter.fromXml(stream.konsumeXml())
        val feed = pair.first
        val items = pair.second

        with(feed) {
            assertEquals(name, "Hacker News")
            assertEquals(url, "https://news.ycombinator.com/feed/")
            assertEquals(siteUrl, "https://news.ycombinator.com/")
            assertEquals(description, "Links for the intellectually curious, ranked by readers.")
        }

        with(items[0]) {
            assertEquals(items.size, 7)
            assertEquals(title, "Africa declared free of wild polio")
            assertEquals(link, "https://www.bbc.com/news/world-africa-53887947")
            assertEquals(pubDate, DateUtils.parse("Tue, 25 Aug 2020 17:15:49 +0000"))
            assertEquals(author, "Author 1")
            assertEquals(description, "<a href=\"https://news.ycombinator.com/item?id=24273602\">Comments</a>")
            assertEquals(remoteId, "https://www.bbc.com/news/world-africa-53887947")
        }
    }


    @Test
    fun nullTitleTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_feed_special_cases.xml")

        assertThrows(ParseException::class.java) {
            adapter.fromXml(stream.konsumeXml())
        }
    }

    @Test
    fun otherNamespacesTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_other_namespaces.xml")
        val item = adapter.fromXml(stream.konsumeXml()).second[0]

        assertEquals(item.remoteId, "guid")
        assertEquals(item.author, "creator 1, creator 2, creator 3, creator 4")
        assertEquals(item.pubDate, DateUtils.parse("2020-08-05T14:03:48Z"))
        assertEquals(item.content, "content:encoded")
    }

    @Test
    fun noDateTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_no_date.xml")
        val item = adapter.fromXml(stream.konsumeXml()).second[0]

        TestCase.assertNotNull(item.pubDate)
    }

    @Test
    fun noTitleTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_no_title.xml")

        val exception = assertThrows(ParseException::class.java) {
            adapter.fromXml(stream.konsumeXml())
        }

        assertTrue(exception.message!!.contains("Item title is required"))
    }

    @Test
    fun noLinkTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_no_link.xml")

        val exception = assertThrows(ParseException::class.java) {
            adapter.fromXml(stream.konsumeXml())
        }

        assertTrue(exception.message!!.contains("Item link is required"))
    }

    @Test
    fun enclosureTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_enclosure.xml")
        val item = adapter.fromXml(stream.konsumeXml()).second[0]

        assertEquals(item.imageLink, "https://image1.jpg")
    }

    @Test
    fun mediaContentTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_media_content.xml")
        val items = adapter.fromXml(stream.konsumeXml())

        assertEquals(items.second[0].imageLink, "https://image1.jpg")
        assertEquals(items.second[1].imageLink, "https://image2.jpg")
    }

    @Test
    fun mediaGroupTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_media_group.xml")
        val item = adapter.fromXml(stream.konsumeXml()).second[0]

        assertEquals(item.imageLink, "https://image1.jpg")
    }
}