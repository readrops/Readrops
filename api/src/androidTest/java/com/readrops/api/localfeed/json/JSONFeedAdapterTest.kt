package com.readrops.api.localfeed.json

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.db.entities.Feed
import com.squareup.moshi.Moshi
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JSONFeedAdapterTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    private val adapter = Moshi.Builder()
            .add(JSONFeedAdapter())
            .build()
            .adapter<Feed>(Feed::class.java)

    @Test
    fun normalCasesTest() {
        val stream = context.assets.open("localfeed/json/json_feed.json")

        val feed = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(feed.name, "News from Flying Meat")
        assertEquals(feed.url, "http://flyingmeat.com/blog/feed.json")
        assertEquals(feed.siteUrl, "http://flyingmeat.com/blog/")
        assertEquals(feed.description, "News from your friends at Flying Meat.")
    }

}