package com.readrops.api.services.freshrss

import com.readrops.api.TestUtils
import com.readrops.api.apiModule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.HttpURLConnection
import java.net.URLEncoder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreshRSSDataSourceTest : KoinTest {

    private lateinit var freshRSSDataSource: FreshRSSDataSource
    private val mockServer = MockWebServer()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(apiModule, module {
            single {
                Retrofit.Builder()
                        .baseUrl("http://localhost:8080/")
                        .client(get())
                        .addConverterFactory(MoshiConverterFactory.create(get(named("freshrssMoshi"))))
                        .build()
                        .create(FreshRSSService::class.java)
            }
        })
    }

    @Before
    fun before() {
        mockServer.start(8080)
        freshRSSDataSource = FreshRSSDataSource(get())
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun loginTest() {
        runBlocking {
            val responseBody = TestUtils.loadResource("services/freshrss/login_response_body")
            mockServer.enqueue(MockResponse()
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setBody(Buffer().readFrom(responseBody)))

            val authString = freshRSSDataSource.login("Login", "Password")
            assertEquals("login/p1f8vmzid4hzzxf31mgx50gt8pnremgp4z8xe44a", authString)

            val request = mockServer.takeRequest()
            val requestBody = request.body.readUtf8()

            assertTrue {
                requestBody.contains("name=\"Email\"") && requestBody.contains("Login")
            }

            assertTrue {
                requestBody.contains("name=\"Passwd\"") && requestBody.contains("Password")
            }
        }
    }

    @Test
    fun writeTokenTest() = runBlocking {
        val responseBody = TestUtils.loadResource("services/freshrss/writetoken_response_body")
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Buffer().readFrom(responseBody)))

        val writeToken = freshRSSDataSource.getWriteToken()

        assertEquals("PMvYZHrnC57cyPLzxFvQmJEGN6KvNmkHCmHQPKG5eznWMXriq13H1nQZg", writeToken)
    }

    @Test
    fun userInfoTest() = runBlocking {

    }

    @Test
    fun foldersTest() = runBlocking {
        val stream = TestUtils.loadResource("services/freshrss/adapters/folders.json")
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Buffer().readFrom(stream)))

        val folders = freshRSSDataSource.getFolders()
        assertTrue { folders.size == 1 }
    }

    @Test
    fun feedsTest() = runBlocking {
        val stream = TestUtils.loadResource("services/freshrss/adapters/feeds.json")
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Buffer().readFrom(stream)))

        val feeds = freshRSSDataSource.getFeeds()
        assertTrue { feeds.size == 1 }
    }

    @Test
    fun itemsTest() = runBlocking {
        val stream = TestUtils.loadResource("services/freshrss/adapters/items.json")
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Buffer().readFrom(stream)))

        val items = freshRSSDataSource.getItems(listOf(FreshRSSDataSource.GOOGLE_READ, FreshRSSDataSource.GOOGLE_STARRED), 100, 21343321321321)
        assertTrue { items.size == 2 }

        val request = mockServer.takeRequest()

        with(request.requestUrl!!) {
            assertEquals(listOf(FreshRSSDataSource.GOOGLE_READ, FreshRSSDataSource.GOOGLE_STARRED), queryParameterValues("xt"))
            assertEquals("100", queryParameter("n"))
            assertEquals("21343321321321", queryParameter("ot"))

        }
    }

    @Test
    fun starredItemsTest() = runBlocking {
        val stream = TestUtils.loadResource("services/freshrss/adapters/items.json")
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Buffer().readFrom(stream)))

        val items = freshRSSDataSource.getStarredItems(100)
        assertTrue { items.size == 2 }

        val request = mockServer.takeRequest()

        assertEquals("100", request.requestUrl!!.queryParameter("n"))
    }

    @Test
    fun getItemsIdsTest() = runBlocking {
        val stream = TestUtils.loadResource("services/freshrss/adapters/items_starred_ids.json")
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(Buffer().readFrom(stream)))

        val ids = freshRSSDataSource.getItemsIds(FreshRSSDataSource.GOOGLE_READ, FreshRSSDataSource.GOOGLE_READING_LIST, 100)
        assertTrue { ids.size == 5 }

        val request = mockServer.takeRequest()
        with(request.requestUrl!!) {
            assertEquals(FreshRSSDataSource.GOOGLE_READ, queryParameter("xt"))
            assertEquals(FreshRSSDataSource.GOOGLE_READING_LIST, queryParameter("s"))
            assertEquals("100", queryParameter("n"))
        }
    }

    @Test
    fun createFeedTest() = runBlocking {
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK))

        freshRSSDataSource.createFeed("token", "https://feed.url")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue { contains("s=${URLEncoder.encode("${FreshRSSDataSource.FEED_PREFIX}https://feed.url", "UTF-8")}") }
            assertTrue { contains("ac=subscribe") }
        }
    }

    @Test
    fun deleteFeedTest() = runBlocking {
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK))

        freshRSSDataSource.deleteFeed("token", "https://feed.url")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue { contains("s=${URLEncoder.encode("${FreshRSSDataSource.FEED_PREFIX}https://feed.url", "UTF-8")}") }
            assertTrue { contains("ac=unsubscribe") }
        }
    }

    @Test
    fun updateFeedTest() = runBlocking {
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK))

        freshRSSDataSource.updateFeed("token", "https://feed.url", "title", "folderId")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue { contains("s=${URLEncoder.encode("${FreshRSSDataSource.FEED_PREFIX}https://feed.url", "UTF-8")}") }
            assertTrue { contains("t=title") }
            assertTrue { contains("a=folderId") }
            assertTrue { contains("ac=edit") }
        }
    }

    @Test
    fun createFolderTest() = runBlocking {
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK))

        freshRSSDataSource.createFolder("token", "folder")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue { contains("a=${URLEncoder.encode("${FreshRSSDataSource.FOLDER_PREFIX}folder", "UTF-8")}") }
        }
    }

    @Test
    fun updateFolderTest() = runBlocking {
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK))

        freshRSSDataSource.updateFolder("token", "folderId", "folder")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue { contains("s=folderId") }
            assertTrue { contains("dest=${URLEncoder.encode("${FreshRSSDataSource.FOLDER_PREFIX}folder", "UTF-8")}") }
        }
    }

    @Test
    fun deleteFolderTest() = runBlocking {
        mockServer.enqueue(MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK))

        freshRSSDataSource.deleteFolder("token", "folderId")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue { contains("s=folderId") }
        }
    }
}