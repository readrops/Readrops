package com.readrops.app

import com.readrops.api.utils.AuthInterceptor
import com.readrops.app.utils.HtmlParser
import junit.framework.TestCase
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule

class HtmlParserTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module {
            single { OkHttpClient.Builder().addInterceptor(get<AuthInterceptor>()).build() }
            single { AuthInterceptor() }
        })
    }

/*
    @Test
    fun getFeedLinkTest() {
        val url = "https://github.com/readrops/Readrops"
        val parsingResult = ParsingResult("https://github.com/readrops/Readrops/commits/develop.atom",
                "Recent Commits to Readrops:develop")

        val parsingResultList = mutableListOf(parsingResult)

        val parsingResultList1 = HtmlParser.getFeedLink(url)
        Assert.assertEquals(parsingResultList, parsingResultList1)
    }
*/

    @Test
    fun getFaviconLinkTest() {
        val url = "https://github.com/readrops/Readrops"

        TestCase.assertEquals("https://github.com/fluidicon.png", HtmlParser.getFaviconLink(url))
    }
}