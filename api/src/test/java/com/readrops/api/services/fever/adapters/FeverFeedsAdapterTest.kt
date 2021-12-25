package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.readrops.db.entities.Feed
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.Buffer
import org.junit.Test
import kotlin.test.assertEquals

class FeverFeedsAdapterTest {

    val adapter = Moshi.Builder()
        .add(FeverFeedsAdapter())
        .build()
        .adapter<List<Feed>>(Types.newParameterizedType(List::class.java, Feed::class.java))!!

    @Test
    fun validFeedsTest() {
        val stream = TestUtils.loadResource("services/fever/feeds.json")

        val feeds = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(feeds.size, 1)

        with(feeds[0]) {
            assertEquals(name, "xda-developers")
            assertEquals(url, "https://www.xda-developers.com/feed/")
            assertEquals(siteUrl, "https://www.xda-developers.com/")
            assertEquals(remoteId, "32")
        }
    }
}