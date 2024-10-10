package com.readrops.api.localfeed.rss1

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.TestUtils
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.db.util.DateUtils
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RSS1AdapterTest {

    private val adapter = RSS1FeedAdapter()

    @Test
    fun normalCaseTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_feed.xml")

        val pair = adapter.fromXml(stream.konsumeXml())
        val feed = pair.first
        val items = pair.second

        with(feed) {
            assertEquals(name, "Slashdot")
            assertEquals(url, "https://slashdot.org/")
            assertEquals(siteUrl, "https://slashdot.org/")
            assertEquals(description, "News for nerds, stuff that matters")
        }

        with(items[0]) {
            assertEquals(items.size, 4)
            assertEquals(title, "Google Expands its Flutter Development Kit To Windows Apps")
            assertEquals(link!!.trim(), "https://developers.slashdot.org/story/20/09/23/1616231/google-expands-" +
                    "its-flutter-development-kit-to-windows-apps?utm_source=rss1.0mainlinkanon&utm_medium=feed")
            assertEquals(remoteId!!.trim(), "https://developers.slashdot.org/story/20/09/23/1616231/google-expands-" +
                    "its-flutter-development-kit-to-windows-apps?utm_source=rss1.0mainlinkanon&utm_medium=feed")
            assertEquals(pubDate, DateUtils.parse("2020-09-23T16:15:00+00:00"))
            assertEquals(author, "msmash")
            assertNotNull(description)
            assertEquals(content, "content:encoded")
        }
    }

    @Test
    fun specialCasesTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_items_special_cases.xml")

        val item = adapter.fromXml(stream.konsumeXml()).second[0]

        TestCase.assertEquals(item.author, "msmash, creator 2, creator 3, creator 4, ...")
        TestCase.assertEquals(item.link, "https://news.slashdot.org/story/20/09/23/1420240/a-new-york-clock-" +
                "that-told-time-now-tells-the-time-remaining?utm_source=rss1.0mainlinkanon&utm_medium=feed")
    }

    @Test
    fun nullDateTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_items_no_date.xml")

        val item = adapter.fromXml(stream.konsumeXml()).second[0]
        TestCase.assertNotNull(item.pubDate)
    }

    @Test
    fun nullTitleTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_items_no_title.xml")

        val exception = assertThrows(ParseException::class.java) {
            adapter.fromXml(stream.konsumeXml())
        }

        assertTrue(exception.message!!.contains("Item title is required"))
    }

    @Test
    fun nullLinkTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_items_no_link.xml")

        val exception = assertThrows(ParseException::class.java) {
            adapter.fromXml(stream.konsumeXml())
        }

        assertTrue(exception.message!!.contains("RSS1 link or about element is required"))
    }
}