package com.readrops.api.localfeed.atom

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.TestUtils
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.db.util.DateUtils
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ATOMAdapterTest {

    private val adapter = ATOMFeedAdapter()

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_items.xml")

        val pair = adapter.fromXml(stream.konsumeXml())
        val feed = pair.first
        val items = pair.second

        with(feed) {
            assertEquals(name, "Recent Commits to Readrops:develop")
            assertEquals(url, "https://github.com/readrops/Readrops/commits/develop.atom")
            assertEquals(siteUrl, "https://github.com/readrops/Readrops/commits/develop")
            assertEquals(description, "Here is a subtitle")
        }

        with(items[0]) {
            assertEquals(items.size, 4)
            assertEquals(title, "Add an option to open item url in custom tab")
            assertEquals(link, "https://github.com/readrops/Readrops/commit/c15f093a1bc4211e85f8d1817c9073e307afe5ac")
            assertEquals(pubDate, DateUtils.parse("2020-09-06T21:09:59Z"))
            assertEquals(author, "Shinokuni")
            assertEquals(description, "Summary")
            assertEquals(remoteId, "tag:github.com,2008:Grit::Commit/c15f093a1bc4211e85f8d1817c9073e307afe5ac")
            TestCase.assertNotNull(content)
        }
    }

    @Test
    fun noDateTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_items_no_date.xml")

        val item = adapter.fromXml(stream.konsumeXml()).second[0]
        TestCase.assertNotNull(item.pubDate)
    }

    @Test
    fun noTitleTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_items_no_title.xml")

        val exception = assertThrows(ParseException::class.java) {
            adapter.fromXml(stream.konsumeXml())
        }

        assertTrue(exception.message!!.contains("Item title is required"))
    }

    @Test
    fun noLinkTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_items_no_link.xml")

        val exception = assertThrows(ParseException::class.java) {
            adapter.fromXml(stream.konsumeXml())
        }

        assertTrue(exception.message!!.contains("Item link is required"))
    }
}