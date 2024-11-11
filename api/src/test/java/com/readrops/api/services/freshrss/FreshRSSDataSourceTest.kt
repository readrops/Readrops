package com.readrops.api.services.freshrss

import com.readrops.api.TestUtils
import com.readrops.api.apiModule
import com.readrops.api.enqueueOK
import com.readrops.api.enqueueOKStream
import com.readrops.api.okResponseWithBody
import com.readrops.api.services.SyncType
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
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
    fun loginTest() = runTest {
        val responseBody = TestUtils.loadResource("services/freshrss/login_response_body")
        mockServer.enqueueOKStream(responseBody)

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

    @Test
    fun writeTokenTest() = runTest {
        val responseBody = TestUtils.loadResource("services/freshrss/writetoken_response_body")
        mockServer.enqueueOKStream(responseBody)

        val writeToken = freshRSSDataSource.getWriteToken()

        assertEquals("PMvYZHrnC57cyPLzxFvQmJEGN6KvNmkHCmHQPKG5eznWMXriq13H1nQZg", writeToken)
    }

    @Test
    fun userInfoTest() = runTest {
        val responseBody = TestUtils.loadResource("services/freshrss/adapters/user_info.json")
        mockServer.enqueueOKStream(responseBody)

        val userInfo = freshRSSDataSource.getUserInfo()

        assertEquals("test", userInfo.userName)
    }

    @Test
    fun foldersTest() = runTest {
        val stream = TestUtils.loadResource("services/freshrss/adapters/folders.json")
        mockServer.enqueueOKStream(stream)

        val folders = freshRSSDataSource.getFolders()
        assertTrue { folders.size == 1 }
    }

    @Test
    fun feedsTest() = runTest {
        val stream = TestUtils.loadResource("services/freshrss/adapters/feeds.json")
        mockServer.enqueueOKStream(stream)

        val feeds = freshRSSDataSource.getFeeds()
        assertTrue { feeds.size == 1 }
    }

    @Test
    fun itemsTest() = runTest {
        val stream = TestUtils.loadResource("services/freshrss/adapters/items.json")
        mockServer.enqueueOKStream(stream)

        val items = freshRSSDataSource.getItems(
            excludeTargets = listOf(
                FreshRSSDataSource.GOOGLE_READ,
                FreshRSSDataSource.GOOGLE_STARRED
            ),
            max = 100,
            lastModified = 21343321321321
        )
        assertTrue { items.size == 2 }

        val request = mockServer.takeRequest()

        with(request.requestUrl!!) {
            assertEquals(
                listOf(FreshRSSDataSource.GOOGLE_READ, FreshRSSDataSource.GOOGLE_STARRED),
                queryParameterValues("xt")
            )
            assertEquals("100", queryParameter("n"))
            assertEquals("21343321321321", queryParameter("ot"))

        }
    }

    @Test
    fun starredItemsTest() = runTest {
        val stream = TestUtils.loadResource("services/freshrss/adapters/items.json")
        mockServer.enqueueOKStream(stream)

        val items = freshRSSDataSource.getStarredItems(100)
        assertTrue { items.size == 2 }

        val request = mockServer.takeRequest()

        assertEquals("100", request.requestUrl!!.queryParameter("n"))
    }

    @Test
    fun getItemsIdsTest() = runTest {
        val stream = TestUtils.loadResource("services/freshrss/adapters/items_starred_ids.json")
        mockServer.enqueueOKStream(stream)

        val ids = freshRSSDataSource.getItemsIds(
            excludeTarget = FreshRSSDataSource.GOOGLE_READ,
            includeTarget = FreshRSSDataSource.GOOGLE_READING_LIST,
            max = 100
        )
        assertTrue { ids.size == 5 }

        val request = mockServer.takeRequest()
        with(request.requestUrl!!) {
            assertEquals(FreshRSSDataSource.GOOGLE_READ, queryParameter("xt"))
            assertEquals(FreshRSSDataSource.GOOGLE_READING_LIST, queryParameter("s"))
            assertEquals("100", queryParameter("n"))
        }
    }

    @Test
    fun createFeedTest() = runTest {
        mockServer.enqueueOK()

        freshRSSDataSource.createFeed("token", "https://feed.url", "feed/1")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue { contains("a=feed%2F1") }
            assertTrue {
                contains(
                    "s=${
                        URLEncoder.encode(
                            "${FreshRSSDataSource.FEED_PREFIX}https://feed.url", "UTF-8"
                        )
                    }"
                )
            }
            assertTrue { contains("ac=subscribe") }
        }
    }

    @Test
    fun deleteFeedTest() = runTest {
        mockServer.enqueueOK()

        freshRSSDataSource.deleteFeed("token", "https://feed.url")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue {
                contains(
                    "s=${
                        URLEncoder.encode(
                            "${FreshRSSDataSource.FEED_PREFIX}https://feed.url",
                            "UTF-8"
                        )
                    }"
                )
            }
            assertTrue { contains("ac=unsubscribe") }
        }
    }

    @Test
    fun updateFeedTest() = runTest {
        mockServer.enqueueOK()

        freshRSSDataSource.updateFeed("token", "https://feed.url", "title", "folderId")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue {
                contains(
                    "s=${
                        URLEncoder.encode(
                            "${FreshRSSDataSource.FEED_PREFIX}https://feed.url",
                            "UTF-8"
                        )
                    }"
                )
            }
            assertTrue { contains("t=title") }
            assertTrue { contains("a=folderId") }
            assertTrue { contains("ac=edit") }
        }
    }

    @Test
    fun createFolderTest() = runTest {
        mockServer.enqueueOK()

        freshRSSDataSource.createFolder("token", "folder")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue {
                contains(
                    "a=${
                        URLEncoder.encode(
                            "${FreshRSSDataSource.FOLDER_PREFIX}folder",
                            "UTF-8"
                        )
                    }"
                )
            }
        }
    }

    @Test
    fun updateFolderTest() = runTest {
        mockServer.enqueueOK()

        freshRSSDataSource.updateFolder("token", "folderId", "folder")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue { contains("s=folderId") }
            assertTrue {
                contains(
                    "dest=${
                        URLEncoder.encode(
                            "${FreshRSSDataSource.FOLDER_PREFIX}folder",
                            "UTF-8"
                        )
                    }"
                )
            }
        }
    }

    @Test
    fun deleteFolderTest() = runTest {
        mockServer.enqueueOK()

        freshRSSDataSource.deleteFolder("token", "folderId")
        val request = mockServer.takeRequest()

        with(request.body.readUtf8()) {
            assertTrue { contains("T=token") }
            assertTrue { contains("s=folderId") }
        }
    }

    @Test
    fun initialSyncTest() = runTest {
        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                with(request.path!!) {
                    return when {
                        contains("tag/list") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/freshrss/adapters/folders.json"))
                        }

                        contains("subscription/list") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/freshrss/adapters/feeds.json"))
                        }

                        // items
                        contains("contents/user/-/state/com.google/reading-list") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/freshrss/adapters/items.json"))
                        }

                        // starred items
                        contains("contents/user/-/state/com.google/starred") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/freshrss/adapters/items.json"))
                        }

                        // unread ids & starred ids
                        contains("stream/items/ids") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/freshrss/adapters/items_starred_ids.json"))
                        }

                        else -> MockResponse().setResponseCode(404)
                    }
                }
            }
        }

        val result =
            freshRSSDataSource.synchronize(SyncType.INITIAL_SYNC, FreshRSSSyncData(), "writeToken")

        with(result) {
            assertEquals(1, folders.size)
            assertEquals(1, feeds.size)
            assertEquals(2, items.size)
            assertEquals(2, starredItems.size)
            assertEquals(5, unreadIds.size)
            assertEquals(5, starredIds.size)
        }
    }

    @Test
    fun classicSync() = runTest {
        var setItemState = 0
        val ids = listOf("1", "2", "3", "4")
        val lastModified = 10L

        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                with(request.path!!) {
                    // printing request path before anything prevents a request being ignored and the test fail, I don't really know why
                    println("request: ${request.path}")
                    return when {
                        contains("0/edit-tag") -> {
                            setItemState++
                            MockResponse().setResponseCode(200)
                        }

                        contains("tag/list") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/freshrss/adapters/folders.json"))
                        }

                        contains("subscription/list") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/freshrss/adapters/feeds.json"))
                        }

                        // items
                        contains("contents/user/-/state/com.google/reading-list") -> {
                            assertTrue { request.path!!.contains("ot=$lastModified") }
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/freshrss/adapters/items.json"))
                        }

                        // unread & read ids
                        contains("stream/items/ids") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/freshrss/adapters/items_starred_ids.json"))
                        }

                        else -> MockResponse().setResponseCode(404)
                    }
                }
            }
        }

        val result = freshRSSDataSource.synchronize(
            syncType = SyncType.CLASSIC_SYNC,
            syncData = FreshRSSSyncData(
                lastModified = 10L,
                readIds = ids,
                unreadIds = ids,
                starredIds = ids,
                unstarredIds = ids
            ),
            writeToken = "writeToken"
        )

        with(result) {
            assertEquals(4, setItemState)
            assertEquals(1, folders.size)
            assertEquals(1, feeds.size)
            assertEquals(2, items.size)
            assertEquals(5, unreadIds.size)
            assertEquals(5, readIds.size)
            assertEquals(5, starredIds.size)
        }
    }
}