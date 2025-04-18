package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.squareup.moshi.Moshi
import okio.Buffer
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeverAPIAdapterTest {

    private val adapter = Moshi.Builder()
        .add(Boolean::class.java, FeverAPIAdapter())
        .build()
        .adapter(Boolean::class.java)

    @Test
    fun authenticatedTest() {
        val stream = TestUtils.loadResource("services/fever/successful_auth.json")

        assertTrue { adapter.fromJson(Buffer().readFrom(stream))!! }
    }

    @Test
    fun unauthenticatedTest() {
        val stream = TestUtils.loadResource("services/fever/unsuccessful_auth.json")

        assertFalse { adapter.fromJson(Buffer().readFrom(stream))!! }
    }
}