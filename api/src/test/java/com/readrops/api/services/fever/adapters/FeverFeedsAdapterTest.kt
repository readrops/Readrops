package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.squareup.moshi.Moshi
import okio.Buffer
import org.junit.Test
import kotlin.test.assertEquals

class FeverFeedsAdapterTest {

    val adapter = Moshi.Builder()
        .add(FeverFeeds::class.java, FeverFeedsAdapter())
        .build()
        .adapter(FeverFeeds::class.java)!!

    @Test
    fun validFeedsTest() {
        val stream = TestUtils.loadResource("services/fever/feeds.json")

        val feverFeeds = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(feverFeeds.feeds.size, 1)

        with(feverFeeds.feeds[0]) {
            assertEquals(name, "xda-developers")
            assertEquals(url, "https://www.xda-developers.com/feed/")
            assertEquals(siteUrl, "https://www.xda-developers.com/")
            assertEquals(remoteId, "32")
        }

        with(feverFeeds.feedsGroups.entries.first()) {
            assertEquals(key, 3)
            assertEquals(value, listOf(5, 4))
        }

        with(feverFeeds.favicons.entries.first()) {
            assertEquals(30, key)
            assertEquals("32", value)
        }
    }
}