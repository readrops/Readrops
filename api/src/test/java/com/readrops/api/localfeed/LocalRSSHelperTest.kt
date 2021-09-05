package com.readrops.api.localfeed

import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.konsumeXml
import junit.framework.TestCase.*
import org.junit.Test
import java.io.ByteArrayInputStream

class LocalRSSHelperTest {

    @Test
    fun standardContentTypesTest() {
        assertEquals(LocalRSSHelper.getRSSType("application/rdf+xml"),
                LocalRSSHelper.RSSType.RSS_1)
        assertEquals(LocalRSSHelper.getRSSType("application/rss+xml"),
                LocalRSSHelper.RSSType.RSS_2)
        assertEquals(LocalRSSHelper.getRSSType("application/atom+xml"),
                LocalRSSHelper.RSSType.ATOM)
        assertEquals(LocalRSSHelper.getRSSType("application/json"),
                LocalRSSHelper.RSSType.JSONFEED)
        assertEquals(LocalRSSHelper.getRSSType("application/feed+json"),
                LocalRSSHelper.RSSType.JSONFEED)
    }

    @Test
    fun nonSupportedContentTypesTest() {
        assertEquals(LocalRSSHelper.getRSSType("application/xml"),
                LocalRSSHelper.RSSType.UNKNOWN)
        assertEquals(LocalRSSHelper.getRSSType("text/xml"),
                LocalRSSHelper.RSSType.UNKNOWN)
        assertEquals(LocalRSSHelper.getRSSType("text/html"),
                LocalRSSHelper.RSSType.UNKNOWN)
    }

    @Test
    fun guessRSSTypeRSS1Test() {
        val xml = """
            <RDF>
                <title></title>
                <description></description>
            </RDF>
        """.trimIndent()

        val konsumer = xml.konsumeXml().nextElement(Names.of("RDF"))!!

        assertEquals(LocalRSSHelper.guessRSSType(konsumer), LocalRSSHelper.RSSType.RSS_1)
    }

    @Test
    fun guessRSSTypeATOMTest() {
        val xml = """
            <feed>
                <title></title>
                <description></description>
            </feed>
        """.trimIndent()

        val konsumer = xml.konsumeXml().nextElement(Names.of("feed"))!!

        assertEquals(LocalRSSHelper.guessRSSType(konsumer), LocalRSSHelper.RSSType.ATOM)

    }

    @Test
    fun guessRSSTypeRSS2Test() {
        val xml = """
            <rss>
                <title></title>
                <description></description>
            </rss>
        """.trimIndent()

        val konsumer = xml.konsumeXml().nextElement(Names.of("rss"))!!

        assertEquals(LocalRSSHelper.guessRSSType(konsumer), LocalRSSHelper.RSSType.RSS_2)

    }

    @Test
    fun isRSSTypeTest() {
        assertTrue(LocalRSSHelper.isRSSType("application/rss+xml"))
    }

    @Test
    fun isRSSTypeNullCaseTest() {
        assertFalse(LocalRSSHelper.isRSSType(null))
    }


}