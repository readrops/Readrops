package com.readrops.api.localfeed.json

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.ParseException
import com.readrops.db.entities.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import okio.Buffer
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class JSONItemsAdapterTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    private val adapter = Moshi.Builder()
            .add(Types.newParameterizedType(List::class.java, Item::class.java), JSONItemsAdapter())
            .build()
            .adapter<List<Item>>(Types.newParameterizedType(List::class.java, Item::class.java))

    @Test
    fun normalCasesTest() {
        val stream = context.resources.assets.open("localfeed/json/json_feed.json")

        val items = adapter.fromJson(Buffer().readFrom(stream))!!
        val item = items[0]

        assertEquals(items.size, 10)
        assertEquals(item.guid, "http://flyingmeat.com/blog/archives/2017/9/acorn_and_10.13.html")
        assertEquals(item.title, "Acorn and 10.13")
        assertEquals(item.link, "http://flyingmeat.com/blog/archives/2017/9/acorn_and_10.13.html")
        assertEquals(item.pubDate, DateUtils.stringToLocalDateTime("2017-09-25T14:27:27-07:00"))
        assertEquals(item.author, "Author 1")
        assertNotNull(item.content)
    }

    @Test
    fun otherCasesTest() {
        val stream = context.resources.assets.open("localfeed/json/json_items_other_cases.json")

        val item = adapter.fromJson(Buffer().readFrom(stream))!![0]

        assertEquals(item.description, "This is a summary")
        assertEquals(item.content, "content_html")
        assertEquals(item.imageLink, "https://image.com")
        assertEquals(item.author, "Author 1, Author 3, Author 4, Author 5, ...")
    }

    @Test
    fun nullTitleTest() {
        val stream = context.resources.assets.open("localfeed/json/json_items_required_elements.json")

        Assert.assertThrows("Item title is required", ParseException::class.java) { adapter.fromJson(Buffer().readFrom(stream))!![0] }
    }

    @Test
    fun nullLinkTest() {
        val stream = context.resources.assets.open("localfeed/json/json_items_required_elements.json")

        Assert.assertThrows("Item link is required", ParseException::class.java) { adapter.fromJson(Buffer().readFrom(stream))!![1] }
    }

    @Test
    fun nullDateTest() {
        val stream = context.resources.assets.open("localfeed/json/json_items_required_elements.json")

        Assert.assertThrows("Item date is required", ParseException::class.java) { adapter.fromJson(Buffer().readFrom(stream))!![2] }
    }
}