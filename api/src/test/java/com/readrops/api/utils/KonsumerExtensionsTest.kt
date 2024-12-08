package com.readrops.api.utils

import com.gitlab.mvysny.konsumexml.KonsumerException
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.utils.extensions.nonNullText
import com.readrops.api.utils.extensions.nullableText
import com.readrops.api.utils.extensions.nullableTextRecursively
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
  description
</description>
        """.trimIndent()

        xml.konsumeXml().apply {
            val description = child("description") { nonNullText() }
            assertEquals("description\n  description", description)
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
  description
</description>
        """.trimIndent()

        xml.konsumeXml().apply {
            val description = child("description") { nullableText() }
            assertEquals("description\n  description", description)
        }
    }

    @Test
    fun nullableTextRecursivelyNullCaseTest() {
        val xml = """
            <description></description>
        """.trimIndent()

        xml.konsumeXml().apply {
            val description = child("description") { nullableTextRecursively() }
            assertNull(description)
        }
    }

    @Test
    fun nullableTextRecursivelyNonNullCaseTest() {
        val xml = """
            <description>
descrip<a>tion</a>
  description
</description>
        """.trimIndent()

        xml.konsumeXml().apply {
            val description = child("description") { nullableTextRecursively() }
            assertEquals("description\n  description", description)
        }
    }
}