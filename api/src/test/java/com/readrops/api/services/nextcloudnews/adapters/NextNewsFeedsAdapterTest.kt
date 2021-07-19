package com.readrops.api.services.nextcloudnews.adapters

import com.readrops.api.TestUtils
import com.readrops.db.entities.Feed
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextNewsFeedsAdapterTest {

    private val adapter = Moshi.Builder()
            .add(NextNewsFeedsAdapter())
            .build()
            .adapter<List<Feed>>(Types.newParameterizedType(List::class.java, Feed::class.java))

    @Test
    fun validFeedsTest() {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/feeds.json")

        val feeds = adapter.fromJson(Buffer().readFrom(stream))!!
        val feed1 = feeds[0]

        assertEquals(feed1.name, "Krebs on Security")
        assertEquals(feed1.url, "https://krebsonsecurity.com/feed/")
        assertEquals(feed1.siteUrl, "https://krebsonsecurity.com/")
        assertEquals(feed1.remoteId, "3")
        assertNull(feed1.remoteFolderId)
        assertEquals(feed1.iconUrl, "https://krebsonsecurity.com/favicon.ico")

        val feed2 = feeds[1]
        assertNull(feed2.iconUrl)
        assertNull(feed2.siteUrl)
        assertNull(feed2.remoteFolderId)

        val feed3 = feeds[2]
        assertEquals(feed3.name, "krebsonsecurity.com")
        assertEquals(feed3.remoteFolderId, "5")
    }
}