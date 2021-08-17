package com.readrops.api.localfeed.atom

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.TestUtils
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.exceptions.ParseException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class ATOMItemsAdapterTest {

    private val adapter = ATOMItemsAdapter()

    @get:Rule
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_items.xml")

        val items = adapter.fromXml(stream.konsumeXml())
        val item = items[0]

        assertEquals(items.size, 4)
        assertEquals(item.title, "Add an option to open item url in custom tab")
        assertEquals(item.link, "https://github.com/readrops/Readrops/commit/c15f093a1bc4211e85f8d1817c9073e307afe5ac")
        assertEquals(item.pubDate, DateUtils.parse("2020-09-06T21:09:59Z"))
        assertEquals(item.author, "Shinokuni")
        assertEquals(item.description, "Summary")
        assertEquals(item.guid, "tag:github.com,2008:Grit::Commit/c15f093a1bc4211e85f8d1817c9073e307afe5ac")
        assertNotNull(item.content)
    }

    @Test
    fun noDateTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_items_no_date.xml")

        val item = adapter.fromXml(stream.konsumeXml()).first()
        assertNotNull(item.pubDate)
    }

    @Test
    fun noTitleTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_items_no_title.xml")

        expectedException.expect(ParseException::class.java)
        expectedException.expectMessage("Item title is required")

        adapter.fromXml(stream.konsumeXml())
    }

    @Test
    fun noLinkTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_items_no_link.xml")

        expectedException.expect(ParseException::class.java)
        expectedException.expectMessage("Item link is required")

        adapter.fromXml(stream.konsumeXml())
    }

}