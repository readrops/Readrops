package com.readrops.api.services.nextcloudnews.adapters

import com.readrops.api.TestUtils
import com.readrops.db.entities.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.joda.time.LocalDateTime
import org.junit.Test

class NextcloudNewsItemsAdapterTest {

    private val adapter = Moshi.Builder()
            .add(Types.newParameterizedType(List::class.java, Item::class.java), NextcloudNewsItemsAdapter())
            .build()
            .adapter<List<Item>>(Types.newParameterizedType(List::class.java, Item::class.java))

    @Test
    fun validItemsTest() {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/items.json")

        val items = adapter.fromJson(Buffer().readFrom(stream))!!
        val item = items[0]

        with(item) {
            assertEquals(remoteId, "3443")
            assertEquals(link, "http://grulja.wordpress.com/2013/04/29/plasma-nm-after-the-solid-sprint/")
            assertEquals(title, "Plasma-nm after the solid sprint")
            assertEquals(author, "Jan Grulich (grulja)")
            assertEquals(content, "<p>At first I have to say...</p>")
            assertEquals(feedRemoteId, "67")
            assertEquals(isRead, false)
            assertEquals(isStarred, false)
            assertEquals(pubDate, LocalDateTime(1367270544000))
            assertEquals(imageLink, null)
        }

        with(items[1]) {
            assertEquals(imageLink, "https://test.org/image.jpg")
        }
    }


}