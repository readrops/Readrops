package com.readrops.api.localfeed.atom

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.exceptions.ParseException
import junit.framework.TestCase.*
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ATOMItemsAdapterTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    private val adapter = ATOMItemsAdapter()

    @Test
    fun normalCasesTest() {
        val stream = context.resources.assets.open("localfeed/atom/atom_items.xml")

        val items = adapter.fromXml(stream)
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
        val stream = context.resources.assets.open("localfeed/atom/atom_items_no_date.xml")

        val item = adapter.fromXml(stream).first()
        assertNotNull(item.pubDate)
    }

    @Test
    fun noTitleTest() {
        val stream = context.resources.assets.open("localfeed/atom/atom_items_no_title.xml")

        val exception = Assert.assertThrows(ParseException::class.java) { adapter.fromXml(stream) }
        assertTrue(exception.message!!.contains("Item title is required"))
    }

    @Test
    fun noLinkTest() {
        val stream = context.resources.assets.open("localfeed/atom/atom_items_no_link.xml")

        val exception = Assert.assertThrows(ParseException::class.java) { adapter.fromXml(stream) }
        assertTrue(exception.message!!.contains("Item link is required"))
    }

}