package com.readrops.api.localfeed.rss1

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.ParseException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RSS1ItemsAdapterTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    private val adapter = RSS1ItemsAdapter()

    @Test
    fun normalCasesTest() {
        val stream = context.resources.assets.open("localfeed/rss1/rss1_feed.xml")

        val items = adapter.fromXml(stream)
        val item = items.first()

        assertEquals(items.size, 4)
        assertEquals(item.title, "Google Expands its Flutter Development Kit To Windows Apps")
        assertEquals(item.link.trim(), "https://developers.slashdot.org/story/20/09/23/1616231/google-expands-" +
                "its-flutter-development-kit-to-windows-apps?utm_source=rss1.0mainlinkanon&utm_medium=feed")
        assertEquals(item.guid.trim(), "https://developers.slashdot.org/story/20/09/23/1616231/google-expands-" +
                "its-flutter-development-kit-to-windows-apps?utm_source=rss1.0mainlinkanon&utm_medium=feed")
        assertEquals(item.pubDate, DateUtils.stringToLocalDateTime("2020-09-23T16:15:00+00:00"))
        assertEquals(item.author, "msmash")
        assertNotNull(item.description)
        assertEquals(item.content, "content:encoded")
    }

    @Test
    fun specialCasesTest() {
        val stream = context.resources.assets.open("localfeed/rss1/rss1_items_special_cases.xml")

        val item = adapter.fromXml(stream).first()

        assertEquals(item.author, "msmash, creator 2, creator 3, creator 4, ...")
        assertEquals(item.link, "https://news.slashdot.org/story/20/09/23/1420240/a-new-york-clock-" +
                "that-told-time-now-tells-the-time-remaining?utm_source=rss1.0mainlinkanon&utm_medium=feed")
    }

    @Test
    fun nullTitleTest() {
        val stream = context.resources.assets.open("localfeed/rss1/rss1_items_no_title.xml")

        Assert.assertThrows(ParseException::class.java) { adapter.fromXml(stream) }
    }

    @Test
    fun nullLinkTest() {
        val stream = context.resources.assets.open("localfeed/rss1/rss1_items_no_link.xml")

        Assert.assertThrows(ParseException::class.java) { adapter.fromXml(stream) }
    }

    @Test
    fun nullDateTest() {
        val stream = context.resources.assets.open("localfeed/rss1/rss1_items_no_date.xml")

        Assert.assertThrows(ParseException::class.java) { adapter.fromXml(stream) }
    }
}