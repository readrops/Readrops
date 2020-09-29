package com.readrops.api.utils

import com.gitlab.mvysny.konsumexml.KonsumerException
import com.gitlab.mvysny.konsumexml.konsumeXml
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class KonsumerExtensionsTest {

    @Test(expected = KonsumerException::class)
    fun nonNullTextNullCaseTest() {
        val xml = """
            <description></description>
        """.trimIndent()

        xml.konsumeXml().apply {
            child("description") { nonNullText() }
        }
    }

    @Test
    fun nonNullTextNonNullCaseTest() {
        val xml = """
            <description>
description
</description>
        """.trimIndent()

        xml.konsumeXml().apply {
            val description = child("description") { nonNullText() }
            assertEquals(description, "description")
        }
    }

    @Test
    fun nullableTextNullCaseTest() {
        val xml = """
            <description></description>
        """.trimIndent()

        xml.konsumeXml().apply {
            val description = child("description") { nullableText() }
            assertNull(description)
        }
    }

    @Test
    fun nullableTextNonNullCaseTest() {
        val xml = """
            <description>
description
</description>
        """.trimIndent()

        xml.konsumeXml().apply {
            val description = child("description") { nullableText() }
            assertEquals(description, "description")
        }
    }
}