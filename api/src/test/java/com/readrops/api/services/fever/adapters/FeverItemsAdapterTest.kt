package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.readrops.db.entities.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.Buffer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FeverItemsAdapterTest {

    private val adapter = Moshi.Builder()
        .add(FeverItemsAdapter())
        .build()
        .adapter<List<Item>>(Types.newParameterizedType(List::class.java, Item::class.java))

    @Test
    fun validItemsTest() {
        val stream = TestUtils.loadResource("services/fever/items_page2.json")

        val items = adapter.fromJson(Buffer().readFrom(stream))!!

        with(items[0]) {
            assertEquals(title, "FreshRSS 1.9.0")
            assertEquals(author, "Alkarex")
            assertEquals(link, "https://github.com/FreshRSS/FreshRSS/releases/tag/1.9.0")
            assertNotNull(content)
            assertTrue(isStarred)
            assertTrue(isRead)
            assertNotNull(pubDate)
            assertEquals(remoteId, "10")
            assertEquals(feedRemoteId, "2")
        }
    }
}