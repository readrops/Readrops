package com.readrops.api.localfeed

import android.accounts.NetworkErrorException
import com.readrops.api.TestUtils
import com.readrops.api.apiModule
import com.readrops.api.utils.ApiUtils
import com.readrops.api.utils.AuthInterceptor
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.exceptions.UnknownFormatException
import junit.framework.TestCase.*
import okhttp3.Headers
import okhttp3.HttpUrl
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
import org.koin.test.inject
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit


class LocalRSSDataSourceTest : KoinTest {

    private lateinit var url: HttpUrl

    private val mockServer: MockWebServer = MockWebServer()
    private val localRSSDataSource by inject<LocalRSSDataSource>()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(apiModule, module() {
            single() {
                OkHttpClient.Builder()
                        .callTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(1, TimeUnit.HOURS)
                        .addInterceptor(get<AuthInterceptor>())
                        .build()
            }
        })
    }

    @Before
    fun before() {
        mockServer.start(8080)
        url = mockServer.url("/rss")
    }

    @After
    fun tearDown() {
        mockServer.close()
    }

    @Test
    fun successfulQueryTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_feed.xml")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/xml; charset=UTF-8")
                .addHeader(ApiUtils.ETAG_HEADER, "ETag-value")
                .addHeader(ApiUtils.LAST_MODIFIED_HEADER, "Last-Modified")
                .setBody(Buffer().readFrom(stream)))

        val pair = localRSSDataSource.queryRSSResource(url.toString(), null)
        val feed = pair?.first!!

        assertEquals(feed.name, "Hacker News")
        assertEquals(feed.url, "http://localhost:8080/rss")
        assertEquals(feed.siteUrl, "https://news.ycombinator.com/")
        assertEquals(feed.description, "Links for the intellectually curious, ranked by readers.")

        assertEquals(feed.etag, "ETag-value")
        assertEquals(feed.lastModified, "Last-Modified")

        assertEquals(pair.second.size, 7)
    }

    @Test
    fun headersTest() {
        val stream = TestUtils.loadResource("localfeed/rss2/rss_feed.xml")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader("Content-Type", "application/rss+xml; charset=UTF-8")
                .setBody(Buffer().readFrom(stream)))

        val headers = Headers.headersOf(ApiUtils.ETAG_HEADER, "ETag", ApiUtils.LAST_MODIFIED_HEADER, "Last-Modified")
        localRSSDataSource.queryRSSResource(url.toString(), headers)

        val request = mockServer.takeRequest()

        assertEquals(request.headers[ApiUtils.ETAG_HEADER], "ETag")
        assertEquals(request.headers[ApiUtils.LAST_MODIFIED_HEADER], "Last-Modified")
    }

    @Test
    fun jsonFeedTest() {
        val stream = TestUtils.loadResource("localfeed/json/json_feed.json")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/feed+json")
                .setBody(Buffer().readFrom(stream)))

        val pair = localRSSDataSource.queryRSSResource(url.toString(), null)!!

        assertEquals(pair.first.name, "News from Flying Meat")
        assertEquals(pair.second.size, 10)
    }

    @Test
    fun specialCasesAtomTest() {
        val stream = TestUtils.loadResource("localfeed/atom/atom_feed_no_url_siteurl.xml")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/atom+xml")
                .setBody(Buffer().readFrom(stream)))

        val pair = localRSSDataSource.queryRSSResource(url.toString(), null)!!

        assertEquals(pair.first.url, "http://localhost:8080/rss")
        assertEquals(pair.first.siteUrl, "http://localhost")
    }

    @Test
    fun specialCasesRSS1Test() {
        val stream = TestUtils.loadResource("localfeed/rss1/rss1_feed_no_url_siteurl.xml")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/rdf+xml")
                .setBody(Buffer().readFrom(stream)))

        val pair = localRSSDataSource.queryRSSResource(url.toString(), null)!!

        assertEquals(pair.first.url, "http://localhost:8080/rss")
        assertEquals(pair.first.siteUrl, "http://localhost")
    }

    @Test
    fun response304Test() {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_MODIFIED))

        val pair = localRSSDataSource.queryRSSResource(url.toString(), null)

        assertNull(pair)
    }

    @Test(expected = HttpException::class)
    fun response404Test() {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))

        localRSSDataSource.queryRSSResource(url.toString(), null)
    }

    @Test(expected = UnknownFormatException::class)
    fun noContentTypeTest() {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK))

        localRSSDataSource.queryRSSResource(url.toString(), null)
    }

    @Test(expected = ParseException::class)
    fun badContentTypeTest() {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader("Content-Type", ""))

        localRSSDataSource.queryRSSResource(url.toString(), null)
    }

    @Test(expected = UnknownFormatException::class)
    fun badContentTest() {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader("Content-Type", "application/xml")
                .setBody("<html>  </html>"))

        localRSSDataSource.queryRSSResource(url.toString(), null)
    }

    @Test
    fun isUrlResourceSuccessfulTest() {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader("Content-Type", "application/atom+xml; charset=UTF-8"))

        assertTrue(localRSSDataSource.isUrlRSSResource(url.toString()))
    }

    @Test
    fun isUrlRSSResourceFailureTest() {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))

        assertFalse(localRSSDataSource.isUrlRSSResource(url.toString()))
    }

    @Test
    fun isUrlRSSResourceBadContentTypeTest() {
        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader("Content-Type", "application/xml; charset=UTF-8")
                .setBody("<html> </html>"))

        assertFalse(localRSSDataSource.isUrlRSSResource(url.toString()))
    }
}