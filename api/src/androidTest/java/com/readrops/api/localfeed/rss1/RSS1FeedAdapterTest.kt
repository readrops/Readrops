package com.readrops.api.localfeed.rss1

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RSS1FeedAdapterTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    private val adapter = RSS1FeedAdapter()

    @Test
    fun normalCaseTest() {
        val stream = context.resources.assets.open("localfeed/rss1/rss1_feed.xml")

        val feed = adapter.fromXml(stream)

        assertEquals(feed.name, "Slashdot")
        assertEquals(feed.url, "https://slashdot.org/")
        assertEquals(feed.siteUrl, "https://slashdot.org/")
        assertEquals(feed.description, "News for nerds, stuff that matters")
    }
}