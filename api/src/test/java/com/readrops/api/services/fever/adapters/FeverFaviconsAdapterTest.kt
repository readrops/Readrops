package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.Buffer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FeverFaviconsAdapterTest {

    private val adapter = Moshi.Builder()
        .add(FeverFaviconsAdapter())
        .build()
        .adapter<List<Favicon>>(Types.newParameterizedType(List::class.java, Favicon::class.java))


    @Test
    fun validFaviconsTest() {
        val stream = TestUtils.loadResource("services/fever/favicons.json")

        val favicons = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(favicons.size, 3)

        with(favicons[0]) {
            assertEquals(id, "85")
            assertNotNull(data)
        }
    }
}