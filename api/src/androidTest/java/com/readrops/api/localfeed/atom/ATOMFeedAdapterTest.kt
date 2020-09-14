package com.readrops.api.localfeed.atom

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ATOMFeedAdapterTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    private val adapter = ATOMFeedAdapter()

    @Test
    fun normalCasesTest() {
        val stream = context.assets.open("localfeed/atom/atom_feed.xml")

        val feed = adapter.fromXml(stream)

        assertEquals(feed.name, "Recent Commits to Readrops:develop")
        assertEquals(feed.url, "https://github.com/readrops/Readrops/commits/develop.atom")
        assertEquals(feed.siteUrl, "https://github.com/readrops/Readrops/commits/develop")
        assertEquals(feed.description, "Here is a subtitle")
    }
}