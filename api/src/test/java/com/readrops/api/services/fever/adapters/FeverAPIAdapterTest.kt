package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.squareup.moshi.Moshi
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals

class FeverAPIAdapterTest {

    private val adapter = Moshi.Builder()
        .add(Boolean::class.java, FeverAPIAdapter())
        .build()
        .adapter(Boolean::class.java)

    @Test
    fun authenticatedTest() {
        val stream = TestUtils.loadResource("services/fever/successful_auth.json")

        val value = adapter.fromJson(Buffer().readFrom(stream))!!
        assertEquals(value, true)
    }
}