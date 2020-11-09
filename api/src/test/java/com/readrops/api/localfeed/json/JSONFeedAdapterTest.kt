package com.readrops.api.localfeed.json

import com.readrops.api.TestUtils
import com.readrops.db.entities.Feed
import com.squareup.moshi.Moshi
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.junit.Test

class JSONFeedAdapterTest {

    private val adapter = Moshi.Builder()
            .add(JSONFeedAdapter())
            .build()
            .adapter(Feed::class.java)

    @Test
    fun normalCasesTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_feed.json")

        val feed = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(feed.name, "News from Flying Meat")
        assertEquals(feed.url, "http://flyingmeat.com/blog/feed.json")
        assertEquals(feed.siteUrl, "http://flyingmeat.com/blog/")
        assertEquals(feed.description, "News from your friends at Flying Meat.")
    }

}