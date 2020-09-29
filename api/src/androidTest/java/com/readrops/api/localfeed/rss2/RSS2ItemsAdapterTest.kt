package com.readrops.api.localfeed.rss2

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.ParseException
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RSS2ItemsAdapterTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    private val adapter = RSS2ItemsAdapter()

    @Test
    fun normalCasesTest() {
        val stream = context.resources.assets.open("localfeed/rss_feed.xml")

        val items = adapter.fromXml(stream)
        assertEquals(items.size, 7)

        val item = items[0]

        assertEquals(item.title, "Africa declared free of wild polio")
        assertEquals(item.link, "https://www.bbc.com/news/world-africa-53887947")
        assertEquals(item.pubDate, DateUtils.stringToLocalDateTime("Tue, 25 Aug 2020 17:15:49 +0000"))
        assertEquals(item.author, "Author 1")
        assertEquals(item.description, "<a href=\"https://news.ycombinator.com/item?id=24273602\">Comments</a>")
        assertEquals(item.guid, "https://www.bbc.com/news/world-africa-53887947")
    }

    @Test
    fun otherNamespacesTest() {
        val stream = context.resources.assets.open("localfeed/rss2/rss_items_other_namespaces.xml")
        val item = adapter.fromXml(stream)[0]

        assertEquals(item.guid, "guid")
        assertEquals(item.author, "creator 1, creator 2, creator 3, creator 4")
        assertEquals(item.pubDate, DateUtils.stringToLocalDateTime("2020-08-05T14:03:48Z"))
        assertEquals(item.content, "content:encoded")
    }

    @Test
    fun noTitleTest() {
        val stream = context.resources.assets.open("localfeed/rss2/rss_items_no_title.xml")
        Assert.assertThrows("Item title is required", ParseException::class.java) { adapter.fromXml(stream) }
    }

    @Test
    fun noLinkTest() {
        val stream = context.resources.assets.open("localfeed/rss2/rss_items_no_link.xml")
        Assert.assertThrows("Item link is required", ParseException::class.java) { adapter.fromXml(stream) }
    }

    @Test
    fun noDateTest() {
        val stream = context.resources.assets.open("localfeed/rss2/rss_items_no_date.xml")
        Assert.assertThrows("Item date is required", ParseException::class.java) { adapter.fromXml(stream) }
    }

    @Test
    fun enclosureTest() {
        val stream = context.resources.assets.open("localfeed/rss2/rss_items_enclosure.xml")
        val item = adapter.fromXml(stream)[0]

        assertEquals(item.imageLink, "https://image1.jpg")
    }

    @Test
    fun mediaContentTest() {
        val stream = context.resources.assets.open("localfeed/rss2/rss_items_media_content.xml")
        val item = adapter.fromXml(stream)[0]

        assertEquals(item.imageLink, "https://image2.jpg")
    }
}