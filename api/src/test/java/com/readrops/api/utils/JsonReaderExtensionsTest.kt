package com.readrops.api.utils

import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableLong
import com.readrops.api.utils.extensions.nextNullableString
import com.squareup.moshi.JsonReader
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import okio.Buffer
import org.junit.Test

class JsonReaderExtensionsTest {

    @Test
    fun nextNullableStringNullCaseTest() {
        val reader = JsonReader.of(Buffer().readFrom("""
            {
                "field": null
            }
        """.trimIndent().byteInputStream()))

        reader.beginObject()
        reader.nextName()

        assertNull(reader.nextNullableString())
        reader.endObject()
    }

    @Test
    fun nextNullableStringEmptyCaseTest() {
        val reader = JsonReader.of(Buffer().readFrom("""
            {
                "field": ""
            }
        """.trimIndent().byteInputStream()))

        reader.beginObject()
        reader.nextName()

        assertNull(reader.nextNullableString())
        reader.endObject()
    }

    @Test
    fun nextNullableValueNormalCaseTest() {
        val reader = JsonReader.of(Buffer().readFrom("""
            {
                "field": "value"
            }
        """.trimIndent().byteInputStream()))

        reader.beginObject()
        reader.nextName()

        assertEquals(reader.nextNullableString(), "value")
        reader.endObject()
    }

    @Test
    fun nextNonEmptyStringTest() {
        val reader = JsonReader.of(Buffer().readFrom("""
            {
                "field": "value"
            }
        """.trimIndent().byteInputStream()))

        reader.beginObject()
        reader.nextName()

        assertEquals(reader.nextNonEmptyString(), "value")
        reader.endObject()
    }

    @Test(expected = ParseException::class)
    fun nextNonEmptyStringEmptyCaseTest() {
        val reader = JsonReader.of(Buffer().readFrom("""
            {
                "field": ""
            }
        """.trimIndent().byteInputStream()))

        reader.beginObject()
        reader.nextName()

        reader.nextNonEmptyString()
    }

    @Test
    fun nextNullableLongNormalCaseTest() {
        val reader = JsonReader.of(Buffer().readFrom("""
            {
                "field": "5555555555555555555"
            }
        """.trimIndent().byteInputStream()))

        reader.beginObject()
        reader.nextName()

        assertEquals(5555555555555555555L, reader.nextNullableLong())
        reader.endObject()
    }

    @Test
    fun nextNullableLongNullCaseTest() {
        val reader = JsonReader.of(Buffer().readFrom("""
            {
                "field": null
            }
        """.trimIndent().byteInputStream()))

        reader.beginObject()
        reader.nextName()

        assertNull(reader.nextNullableLong())
        reader.endObject()
    }
}