package com.readrops.api.services.freshrss.adapters

import com.readrops.api.TestUtils
import com.readrops.db.entities.Feed
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.junit.Test

class FreshRSSFeedsAdapterTest {

    private val adapter = Moshi.Builder()
            .add(FreshRSSFeedsAdapter())
            .build()
            .adapter<List<Feed>>(Types.newParameterizedType(List::class.java, Feed::class.java))

    @Test
    fun validFeedsTest() {
        val stream = TestUtils.loadResource("services/freshrss/adapters/feeds.json")

        val feed = adapter.fromJson(Buffer().readFrom(stream))!!.first()

        with(feed) {
            assertEquals(remoteId, "feed/2")
            assertEquals(name, "FreshRSS @ GitHub")
            assertEquals(url, "https://github.com/FreshRSS/FreshRSS/releases.atom")
            assertEquals(siteUrl, "https://github.com/FreshRSS/FreshRSS/")
            assertEquals(iconUrl, "iconUrl")
        }
    }

}