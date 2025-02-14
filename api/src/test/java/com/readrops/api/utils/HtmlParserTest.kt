package com.readrops.api.utils

import android.nfc.FormatException
import com.readrops.api.TestUtils
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
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

    @Before
    fun before() {
        mockServer.start()
    }

    @After
    fun after() {
        mockServer.shutdown()
    }

    @Test
    fun getFeedLinkTest() = runTest {
        val stream = TestUtils.loadResource("utils/file.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, CONTENT_TYPE_HTML)
                .setBody(Buffer().readFrom(stream))
        )

        val links = HtmlParser.getFeedLink(mockServer.url("/rss").toString(), get())

        assertTrue { links.size == 2 }
        assertTrue { links.all { it.label!!.contains("The Mozilla Blog") } }
    }

    @Test(expected = FormatException::class)
    fun getFeedLinkWithoutHeadTest() = runTest {
        val stream = TestUtils.loadResource("utils/file_without_head.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, CONTENT_TYPE_HTML)
                .setBody(Buffer().readFrom(stream))
        )

        HtmlParser.getFeedLink(mockServer.url("/rss").toString(), get())
    }

    @Test(expected = FormatException::class)
    fun getFeedLinkNoHtmlFileTest() = runTest {
        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/rss+xml")
        )

        HtmlParser.getFeedLink(mockServer.url("/rss").toString(), get())
    }

    @Test
    fun getFaviconLinkTest() = runTest {
        val stream = TestUtils.loadResource("utils/file.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, CONTENT_TYPE_HTML)
                .setBody(Buffer().readFrom(stream))
        )

        val document = HtmlParser.getHTMLHeadFromUrl(mockServer.url("/rss").toString(), get())
        val link = HtmlParser.getFaviconLink(document)
        assertTrue { link!!.contains("apple-touch-icon") }
    }

    @Test
    fun getFaviconLinkWithoutHeadTest() = runTest {
        val stream = TestUtils.loadResource("utils/file_without_icon.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, CONTENT_TYPE_HTML)
                .setBody(Buffer().readFrom(stream))
        )

        val document = HtmlParser.getHTMLHeadFromUrl(mockServer.url("/rss").toString(), get())
        val link = HtmlParser.getFaviconLink(document)
        assertNull(link)
    }

    @Test
    fun getFeedImageLinkTest() = runTest {
        val stream = TestUtils.loadResource("utils/file.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, CONTENT_TYPE_HTML)
                .setBody(Buffer().readFrom(stream))
        )

        val document = HtmlParser.getHTMLHeadFromUrl(mockServer.url("/rss").toString(), get())
        val link = HtmlParser.getFeedImage(document)

        assertEquals(
            "https://blog.mozilla.org/wp-content/blogs.dir/278/files/2021/02/moz_blog_header_som_002_1200x600.jpg",
            link
        )
    }

    @Test
    fun getFeedDescriptionTest() = runTest {
        val stream = TestUtils.loadResource("utils/file.html")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, CONTENT_TYPE_HTML)
                .setBody(Buffer().readFrom(stream))
        )

        val document = HtmlParser.getHTMLHeadFromUrl(mockServer.url("/rss").toString(), get())
        val description = HtmlParser.getFeedDescription(document)

        assertEquals("The Mozilla Blog", description)
    }

    private companion object {
        val CONTENT_TYPE_HTML = "text/html"
    }
}