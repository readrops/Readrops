package com.readrops.api.localfeed.rss2

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.TestUtils
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.exceptions.ParseException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class RSS2ItemsAdapterTest {

    private val adapter = RSS2ItemsAdapter()

    @get:Rule
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/rss_feed.xml")

        val items = adapter.fromXml(stream.konsumeXml())
        val item = items.first()

        assertEquals(items.size, 7)
        assertEquals(item.title, "Africa declared free of wild polio")
        assertEquals(item.link, "https://www.bbc.com/news/world-africa-53887947")
        assertEquals(item.pubDate, DateUtils.parse("Tue, 25 Aug 2020 17:15:49 +0000"))
        assertEquals(item.author, "Author 1")
        assertEquals(item.description, "<a href=\"https://news.ycombinator.com/item?id=24273602\">Comments</a>")
        assertEquals(item.guid, "https://www.bbc.com/news/world-africa-53887947")
    }

    @Test
    fun otherNamespacesTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_other_namespaces.xml")
        val item = adapter.fromXml(stream.konsumeXml()).first()

        assertEquals(item.guid, "guid")
        assertEquals(item.author, "creator 1, creator 2, creator 3, creator 4")
        assertEquals(item.pubDate, DateUtils.parse("2020-08-05T14:03:48Z"))
        assertEquals(item.content, "content:encoded")
    }

    @Test
    fun noDateTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_no_date.xml")
        val item = adapter.fromXml(stream.konsumeXml()).first()

        assertNotNull(item.pubDate)
    }

    @Test
    fun noTitleTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_no_title.xml")

        expectedException.expect(ParseException::class.java)
        expectedException.expectMessage("Item title is required")

        adapter.fromXml(stream.konsumeXml())
    }

    @Test
    fun noLinkTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_no_link.xml")

        expectedException.expect(ParseException::class.java)
        expectedException.expectMessage("Item link is required")

        adapter.fromXml(stream.konsumeXml())
    }

    @Test
    fun enclosureTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_enclosure.xml")
        val item = adapter.fromXml(stream.konsumeXml()).first()

        assertEquals(item.imageLink, "https://image1.jpg")
    }

    @Test
    fun mediaContentTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_media_content.xml")
        val items = adapter.fromXml(stream.konsumeXml())

        assertEquals(items.first().imageLink, "https://image1.jpg")
        assertEquals(items[1].imageLink, "https://image2.jpg")
    }

    @Test
    fun mediaGroupTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_items_media_group.xml")
        val item = adapter.fromXml(stream.konsumeXml()).first()

        assertEquals(item.imageLink, "https://image1.jpg")
    }
}