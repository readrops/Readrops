package com.readrops.app

import com.readrops.app.utils.HtmlParser
import com.readrops.app.utils.ParsingResult
import junit.framework.TestCase
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

class HtmlParserTest {

    @Before
    fun setupKoin() {
        startKoin {
            modules(module {
                single { OkHttpClient() }
            })
        }
    }

    @Test
    fun getFeedLinkTest() {
        val url = "https://github.com/readrops/Readrops"
        val parsingResult = ParsingResult("https://github.com/readrops/Readrops/commits/develop.atom",
                "Recent Commits to Readrops:develop")

        val parsingResultList = mutableListOf(parsingResult)

        val parsingResultList1 = HtmlParser.getFeedLink(url)
        Assert.assertEquals(parsingResultList, parsingResultList1)
    }

    @Test
    fun getFaviconLinkTest() {
        val url = "https://github.com/readrops/Readrops"

        TestCase.assertEquals("https://github.com/fluidicon.png", HtmlParser.getFaviconLink(url))
    }
}