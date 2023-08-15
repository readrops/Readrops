package com.readrops.api.utils

import android.nfc.FormatException
import com.readrops.api.TestUtils
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HtmlParserTest : KoinTest {

    private val mockServer = MockWebServer()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module {
            single {
                OkHttpClient.Builder()
                    .callTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.HOURS)
                    .build()
            }
        })
    }

    @Test
    fun before() {
        mockServer.start()
    }

    @Test
    fun after() {
        mockServer.shutdown()
    }

    @Test
    fun getFeedLinkTest() {
        val stream = TestUtils.loadResource("utils/file.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, ApiUtils.HTML_CONTENT_TYPE)
                .setBody(Buffer().readFrom(stream))
        )

        runBlocking {
            val result =
                HtmlParser.getFeedLink(mockServer.url("/rss").toString(), koinTestRule.koin.get())

            assertTrue { result.size == 1 }
            assertTrue { result.first().url.endsWith("/rss") }
            assertEquals("RSS", result.first().label)

        }
    }

    @Test(expected = FormatException::class)
    fun getFeedLinkWithoutHeadTest() {
        val stream = TestUtils.loadResource("utils/file_without_head.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, ApiUtils.HTML_CONTENT_TYPE)
                .setBody(Buffer().readFrom(stream))
        )

        runBlocking { HtmlParser.getFeedLink(mockServer.url("/rss").toString(), koinTestRule.koin.get()) }
    }

    @Test(expected = FormatException::class)
    fun getFeedLinkNoHtmlFileTest() {
        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/rss+xml"))


        runBlocking { HtmlParser.getFeedLink(mockServer.url("/rss").toString(), koinTestRule.koin.get()) }
    }

    @Test
    fun getFaviconLinkTest() {
        val stream = TestUtils.loadResource("utils/file.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, ApiUtils.HTML_CONTENT_TYPE)
                .setBody(Buffer().readFrom(stream))
        )

        runBlocking {
            val result = HtmlParser.getFaviconLink(mockServer.url("/rss").toString(), koinTestRule.koin.get())

            assertTrue { result!!.contains("favicon.ico") }
        }
    }

    @Test
    fun getFaviconLinkWithoutHeadTest() {
        val stream = TestUtils.loadResource("utils/file_without_icon.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, ApiUtils.HTML_CONTENT_TYPE)
                .setBody(Buffer().readFrom(stream))
        )

        runBlocking {
            val result = HtmlParser.getFaviconLink(mockServer.url("/rss").toString(), koinTestRule.koin.get())

            assertNull(result)
        }
    }
}