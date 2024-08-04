package com.readrops.api.localfeed.json

import com.readrops.api.TestUtils
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.junit.Assert.assertThrows
import org.junit.Test

class JSONFeedAdapterTest {

    private val adapter = Moshi.Builder()
            .add(Types.newParameterizedType(Pair::class.java, Feed::class.java,
                    Types.newParameterizedType(List::class.java, Item::class.java)), JSONFeedAdapter())
            .build()
            .adapter<Pair<Feed, List<Item>>>(Types.newParameterizedType(Pair::class.java, Feed::class.java,
                    Types.newParameterizedType(List::class.java, Item::class.java)))

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_feed.json")

        val pair = adapter.fromJson(Buffer().readFrom(stream))!!
        val feed = pair.first
        val items = pair.second

        with(feed) {
            assertEquals(name, "News from Flying Meat")
            assertEquals(url, "http://flyingmeat.com/blog/feed.json")
            assertEquals(siteUrl, "http://flyingmeat.com/blog/")
            assertEquals(description, "News from your friends at Flying Meat.")
        }

        with(items[0]) {
            assertEquals(items.size, 10)
            assertEquals(remoteId, "http://flyingmeat.com/blog/archives/2017/9/acorn_and_10.13.html")
            assertEquals(title, "Acorn and 10.13")
            assertEquals(link, "http://flyingmeat.com/blog/archives/2017/9/acorn_and_10.13.html")
            assertEquals(pubDate, DateUtils.parse("2017-09-25T14:27:27-07:00"))
            assertEquals(author, "Author 1")
            TestCase.assertNotNull(content)
        }

    }

    @Test
    fun otherCasesTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_items_other_cases.json")

        val item = adapter.fromJson(Buffer().readFrom(stream))!!.second[0]

        assertEquals(item.description, "This is a summary")
        assertEquals(item.content, "content_html")
        assertEquals(item.imageLink, "https://image.com")
        assertEquals(item.author, "Author 1, Author 3, Author 4, Author 5, ...")
    }

    @Test
    fun nullDateTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_items_no_date.json")

        val item = adapter.fromJson(Buffer().readFrom(stream))!!.second[0]
        TestCase.assertNotNull(item.pubDate)
    }

    @Test
    fun nullTitleTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_items_no_title.json")

        val exception = assertThrows(ParseException::class.java) {
            adapter.fromJson(Buffer().readFrom(stream))
        }

        assertEquals("Item title is required", exception.message)
    }

    @Test
    fun nullLinkTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_items_no_link.json")

        val exception = assertThrows(ParseException::class.java) {
            adapter.fromJson(Buffer().readFrom(stream))
        }

        assertEquals("Item link is required", exception.message)
    }

}