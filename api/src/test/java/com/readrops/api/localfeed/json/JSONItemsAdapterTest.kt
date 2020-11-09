package com.readrops.api.localfeed.json

import com.readrops.api.TestUtils
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.db.entities.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import okio.Buffer
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException


class JSONItemsAdapterTest {

    private val adapter = Moshi.Builder()
            .add(Types.newParameterizedType(List::class.java, Item::class.java), JSONItemsAdapter())
            .build()
            .adapter<List<Item>>(Types.newParameterizedType(List::class.java, Item::class.java))

    @get:Rule
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_feed.json")

        val items = adapter.fromJson(Buffer().readFrom(stream))!!
        val item = items.first()

        assertEquals(items.size, 10)
        assertEquals(item.guid, "http://flyingmeat.com/blog/archives/2017/9/acorn_and_10.13.html")
        assertEquals(item.title, "Acorn and 10.13")
        assertEquals(item.link, "http://flyingmeat.com/blog/archives/2017/9/acorn_and_10.13.html")
        assertEquals(item.pubDate, DateUtils.parse("2017-09-25T14:27:27-07:00"))
        assertEquals(item.author, "Author 1")
        assertNotNull(item.content)
    }

    @Test
    fun otherCasesTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_items_other_cases.json")

        val item = adapter.fromJson(Buffer().readFrom(stream))!!.first()

        assertEquals(item.description, "This is a summary")
        assertEquals(item.content, "content_html")
        assertEquals(item.imageLink, "https://image.com")
        assertEquals(item.author, "Author 1, Author 3, Author 4, Author 5, ...")
    }

    @Test
    fun nullDateTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_items_no_date.json")

        val item = adapter.fromJson(Buffer().readFrom(stream))!!.first()
        assertNotNull(item.pubDate)
    }

    @Test
    fun nullTitleTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_items_no_title.json")

        expectedException.expect(ParseException::class.java)
        expectedException.expectMessage("Item title is required")

        adapter.fromJson(Buffer().readFrom(stream))
    }

    @Test
    fun nullLinkTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_items_no_link.json")

        expectedException.expect(ParseException::class.java)
        expectedException.expectMessage("Item link is required")

        adapter.fromJson(Buffer().readFrom(stream))
    }

}