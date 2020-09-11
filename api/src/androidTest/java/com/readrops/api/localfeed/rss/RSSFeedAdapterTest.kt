package com.readrops.api.localfeed.rss

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.api.utils.ParseException
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RSSFeedAdapterTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    private val adapter = RSSFeedAdapter()

    @Test
    fun normalCasesTest() {
        val stream = context.resources.assets.open("localfeed/rss/rss_full_feed.xml")

        val feed = adapter.fromXml(stream)

        assertEquals(feed.name, "Hacker News")
        assertEquals(feed.url, "https://news.ycombinator.com/feed/")
        assertEquals(feed.siteUrl, "https://news.ycombinator.com/")
        assertEquals(feed.description, "Links for the intellectually curious, ranked by readers.")
    }


    @Test(expected = ParseException::class)
    fun nullTitleTest() {
        val stream = context.resources.assets.open("localfeed/rss/rss_feed_special_cases.xml")
        adapter.fromXml(stream)
    }
}