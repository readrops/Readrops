package com.readrops.api.localfeed

import android.accounts.NetworkErrorException
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.api.utils.LibUtils
import com.readrops.api.utils.ParseException
import com.readrops.api.utils.UnknownFormatException
import junit.framework.TestCase.*
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection


@RunWith(AndroidJUnit4::class)
class LocalRSSDataSourceTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var url: HttpUrl

    private val mockServer: MockWebServer = MockWebServer()
    private val localRSSDataSource = LocalRSSDataSource(OkHttpClient())

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
        val stream = context.resources.assets.open("localfeed/rss_feed.xml")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(LibUtils.CONTENT_TYPE_HEADER, "application/xml; charset=UTF-8")
                .addHeader(LibUtils.ETAG_HEADER, "ETag-value")
                .addHeader(LibUtils.LAST_MODIFIED_HEADER, "Last-Modified")
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
        val stream = context.resources.assets.open("localfeed/rss_feed.xml")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader("Content-Type", "application/rss+xml; charset=UTF-8")
                .setBody(Buffer().readFrom(stream)))

        val headers = Headers.headersOf(LibUtils.ETAG_HEADER, "ETag", LibUtils.LAST_MODIFIED_HEADER, "Last-Modified")
        localRSSDataSource.queryRSSResource(url.toString(), headers)

        val request = mockServer.takeRequest()

        assertEquals(request.headers[LibUtils.ETAG_HEADER], "ETag")
        assertEquals(request.headers[LibUtils.LAST_MODIFIED_HEADER], "Last-Modified")
    }

    @Test
    fun jsonFeedTest() {
        val stream = context.resources.assets.open("localfeed/json/json_feed.json")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(LibUtils.CONTENT_TYPE_HEADER, "application/feed+json")
                .setBody(Buffer().readFrom(stream)))

        val pair = localRSSDataSource.queryRSSResource(url.toString(), null)!!

        assertEquals(pair.first.name, "News from Flying Meat")
        assertEquals(pair.second.size, 10)
    }

    @Test
    fun specialCasesAtomTest() {
        val stream = context.resources.assets.open("localfeed/atom/atom_feed_no_url_siteurl.xml")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(LibUtils.CONTENT_TYPE_HEADER, "application/atom+xml")
                .setBody(Buffer().readFrom(stream)))

        val pair = localRSSDataSource.queryRSSResource(url.toString(), null)!!

        assertEquals(pair.first.url, "http://localhost:8080/rss")
        assertEquals(pair.first.siteUrl, "http://localhost")
    }

    @Test
    fun specialCasesRSS1Test() {
        val stream = context.resources.assets.open("localfeed/rss1/rss1_feed_no_url_siteurl.xml")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(LibUtils.CONTENT_TYPE_HEADER, "application/rdf+xml")
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

    @Test(expected = NetworkErrorException::class)
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