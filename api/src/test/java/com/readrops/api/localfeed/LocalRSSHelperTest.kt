package com.readrops.api.localfeed

import junit.framework.TestCase.assertEquals
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
    fun rss1ContentTest() {
        assertEquals(LocalRSSHelper.getRSSContentType(ByteArrayInputStream(
                """<?xml-stylesheet type="text/xsl" media="screen" href="/~d/styles/rss1full.xsl"?>
<?xml-stylesheet type="text/css" media="screen" href="http://rss.slashdot.org/~d/styles/itemcontent.css"?>
<rdf:RDF xmlns:admin="http://webns.net/mvcb/" xmlns:content="http://purl.org/rss/1.0/modules/content/" 
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://purl.org/rss/1.0/"
                """.trimIndent().toByteArray()
        )), LocalRSSHelper.RSSType.RSS_1)
    }


    @Test
    fun rss2ContentTest() {
        assertEquals(LocalRSSHelper.getRSSContentType(ByteArrayInputStream(
                """<rss 
                    xmlns:content="http://purl.org/rss/1.0/modules/content/"
                    xmlns:wfw="http://wellformedweb.org/CommentAPI/"
                    xmlns:dc="http://purl.org/dc/elements/1.1/"
                    version="2.0"
                    xmlns:atom="http://www.w3.org/2005/Atom"
                    xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
                    xmlns:slash="http://purl.org/rss/1.0/modules/slash/">
                </rss>""".toByteArray()
        )), LocalRSSHelper.RSSType.RSS_2)
    }

    @Test
    fun atomContentTest() {
        assertEquals(LocalRSSHelper.getRSSContentType(ByteArrayInputStream(
                """<feed     xmlns="http://www.w3.org/2005/Atom">
                    
                </feed>""".toByteArray()
        )), LocalRSSHelper.RSSType.ATOM)
    }

    @Test
    fun unknownContentTest() {
        assertEquals(LocalRSSHelper.getRSSContentType(ByteArrayInputStream(
                """<html>
                        <body>
                        </body>
               </html>""".trimMargin().toByteArray()
        )), LocalRSSHelper.RSSType.UNKNOWN)

    }
}