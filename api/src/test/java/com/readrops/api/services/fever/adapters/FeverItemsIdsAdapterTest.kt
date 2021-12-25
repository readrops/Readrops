package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.Buffer
import org.junit.Test
import kotlin.test.assertEquals

class FeverItemsIdsAdapterTest {

    private val adapter = Moshi.Builder()
        .add(FeverItemsIdsAdapter())
        .build()
        .adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))

    @Test
    fun validIdsTest() {
        val stream = TestUtils.loadResource("services/fever/itemsIds.json")

        val ids = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(ids.size, 6)
    }
}