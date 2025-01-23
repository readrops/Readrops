package com.readrops.api.services.greader.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.junit.Test

class GReaderItemsIdsAdapterTest {

    private val adapter = Moshi.Builder()
            .add(Types.newParameterizedType(List::class.java, String::class.java), GReaderItemsIdsAdapter())
            .build()
            .adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))

    @Test
    fun validIdsTest() {
        val stream = javaClass.classLoader!!.getResourceAsStream("services/freshrss/adapters/items_starred_ids.json")

        val ids = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(ids, listOf(
                "tag:google.com,2005:reader/item/0005b2c17277b383",
                "tag:google.com,2005:reader/item/0005b2c12d328ae4",
                "tag:google.com,2005:reader/item/0005b2c0781d0737",
                "tag:google.com,2005:reader/item/0005b2bf3852c293",
                "tag:google.com,2005:reader/item/0005b2bebeed9f7f"
        ))
    }
}