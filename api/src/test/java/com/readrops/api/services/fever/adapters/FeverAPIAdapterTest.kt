package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.squareup.moshi.Moshi
import okio.Buffer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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

    @Test
    fun unauthenticatedTest() {
        val stream = TestUtils.loadResource("services/fever/unsuccessful_auth.json")

        val value = adapter.fromJson(Buffer().readFrom(stream))!!
        assertFalse { value }
    }
}