package com.readrops.api.localfeed.rss1

import com.readrops.api.TestUtils
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.exceptions.ParseException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class RSS1ItemsAdapterTest {

    private val adapter = RSS1ItemsAdapter()

    @get:Rule
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_feed.xml")

        val items = adapter.fromXml(stream)
        val item = items.first()

        assertEquals(items.size, 4)
        assertEquals(item.title, "Google Expands its Flutter Development Kit To Windows Apps")
        assertEquals(item.link!!.trim(), "https://developers.slashdot.org/story/20/09/23/1616231/google-expands-" +
                "its-flutter-development-kit-to-windows-apps?utm_source=rss1.0mainlinkanon&utm_medium=feed")
        assertEquals(item.guid!!.trim(), "https://developers.slashdot.org/story/20/09/23/1616231/google-expands-" +
                "its-flutter-development-kit-to-windows-apps?utm_source=rss1.0mainlinkanon&utm_medium=feed")
        assertEquals(item.pubDate, DateUtils.parse("2020-09-23T16:15:00+00:00"))
        assertEquals(item.author, "msmash")
        assertNotNull(item.description)
        assertEquals(item.content, "content:encoded")
    }

    @Test
    fun specialCasesTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_items_special_cases.xml")

        val item = adapter.fromXml(stream).first()

        assertEquals(item.author, "msmash, creator 2, creator 3, creator 4, ...")
        assertEquals(item.link, "https://news.slashdot.org/story/20/09/23/1420240/a-new-york-clock-" +
                "that-told-time-now-tells-the-time-remaining?utm_source=rss1.0mainlinkanon&utm_medium=feed")
    }

    @Test
    fun nullDateTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_items_no_date.xml")

        val item = adapter.fromXml(stream).first()
        assertNotNull(item.pubDate)
    }

    @Test
    fun nullTitleTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_items_no_title.xml")

        expectedException.expect(ParseException::class.java)
        expectedException.expectMessage("Item title is required")

        adapter.fromXml(stream)
    }

    @Test
    fun nullLinkTest() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_items_no_link.xml")

        expectedException.expect(ParseException::class.java)
        expectedException.expectMessage("RSS1 link or about element is required")

        adapter.fromXml(stream)
    }
}