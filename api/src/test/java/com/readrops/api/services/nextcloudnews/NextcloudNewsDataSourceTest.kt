package com.readrops.api.services.nextcloudnews

import com.readrops.api.TestUtils
import com.readrops.api.apiModule
import com.readrops.api.enqueueOK
import com.readrops.api.enqueueOKStream
import com.readrops.api.okResponseWithBody
import com.readrops.api.services.SyncType
import com.readrops.db.entities.account.Account
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NextcloudNewsDataSourceTest : KoinTest {

    private lateinit var nextcloudNewsDataSource: NextcloudNewsDataSource
    private val mockServer = MockWebServer()
    private val moshi = Moshi.Builder()
        .build()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(apiModule, module {
            single {
                Retrofit.Builder()
                    .baseUrl("http://localhost:8080/")
                    .client(get())
                    .addConverterFactory(MoshiConverterFactory.create(get(named("nextcloudNewsMoshi"))))
                    .build()
                    .create(NextcloudNewsService::class.java)
            }
        })
    }

    @Before
    fun before() {
        mockServer.start(8080)
        nextcloudNewsDataSource = NextcloudNewsDataSource(get())
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun loginTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/user.xml")
        val account = Account(login = "login", url = mockServer.url("").toString())

        mockServer.enqueueOKStream(stream)

        val displayName = nextcloudNewsDataSource.login(get(), account)
        val request = mockServer.takeRequest()

        assertTrue { displayName == "Shinokuni" }
        assertTrue { request.headers.contains("OCS-APIRequest" to "true") }
        assertTrue { request.path == "//ocs/v1.php/cloud/users/login" }
    }

    @Test
    fun foldersTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/valid_folder.json")
        mockServer.enqueueOKStream(stream)

        val folders = nextcloudNewsDataSource.getFolders()
        assertTrue { folders.size == 1 }
    }

    @Test
    fun feedsTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/feeds.json")
        mockServer.enqueueOKStream(stream)

        val feeds = nextcloudNewsDataSource.getFeeds()
        assertTrue { feeds.size == 3 }
    }

    @Test
    fun itemsTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/items.json")
        mockServer.enqueueOKStream(stream)

        val type = NextcloudNewsDataSource.ItemQueryType.ALL.value

        val items = nextcloudNewsDataSource.getItems(
            type = type,
            read = false,
            batchSize = 10
        )
        val request = mockServer.takeRequest()

        assertTrue { items.size == 3 }
        with(request.requestUrl!!) {
            assertEquals("$type", queryParameter("type"))
            assertEquals("false", queryParameter("getRead"))
            assertEquals("10", queryParameter("batchSize"))
        }
    }

    @Test
    fun newItemsTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/items.json")
        mockServer.enqueueOKStream(stream)

        val items =
            nextcloudNewsDataSource.getNewItems(1512, NextcloudNewsDataSource.ItemQueryType.ALL)
        val request = mockServer.takeRequest()

        assertTrue { items.size == 3 }
        with(request.requestUrl!!) {
            assertEquals("1512", queryParameter("lastModified"))
            assertEquals("3", queryParameter("type"))
        }
    }

    @Test
    fun createFeedTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/feeds.json")
        mockServer.enqueueOKStream(stream)

        val feeds = nextcloudNewsDataSource.createFeed("https://news.ycombinator.com/rss", 100)
        val request = mockServer.takeRequest()

        assertTrue { feeds.isNotEmpty() }
        with(request.requestUrl!!) {
            assertEquals("https://news.ycombinator.com/rss", queryParameter("url"))
            assertEquals("100", queryParameter("folderId"))
        }
    }

    @Test
    fun deleteFeedTest() = runTest {
        mockServer.enqueueOK()

        nextcloudNewsDataSource.deleteFeed(15)
        val request = mockServer.takeRequest()

        assertTrue { request.path!!.endsWith("/15") }
    }

    @Test
    fun changeFeedFolderTest() = runTest {
        mockServer.enqueueOK()

        nextcloudNewsDataSource.changeFeedFolder(15, 18)
        val request = mockServer.takeRequest()

        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Int::class.javaObjectType
        )
        val adapter = moshi.adapter<Map<String, Int>>(type)
        val body = adapter.fromJson(request.body)!!

        assertTrue { request.path!!.endsWith("/18/move") }
        assertEquals(15, body["folderId"])
    }

    @Test
    fun renameFeedTest() = runTest {
        mockServer.enqueueOK()

        nextcloudNewsDataSource.renameFeed("name", 15)
        val request = mockServer.takeRequest()

        val type =
            Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        val adapter = moshi.adapter<Map<String, String>>(type)
        val body = adapter.fromJson(request.body)!!

        assertTrue { request.path!!.endsWith("/15/rename") }
        assertEquals("name", body["feedTitle"])
    }

    @Test
    fun createFolderTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/valid_folder.json")
        mockServer.enqueueOKStream(stream)

        val folders = nextcloudNewsDataSource.createFolder("folder name")
        val request = mockServer.takeRequest()

        val type =
            Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        val adapter = moshi.adapter<Map<String, String>>(type)
        val body = adapter.fromJson(request.body)!!

        assertTrue { folders.size == 1 }
        assertEquals("folder name", body["name"])
    }

    @Test
    fun renameFolderTest() = runTest {
        mockServer.enqueueOK()

        nextcloudNewsDataSource.renameFolder("new name", 15)
        val request = mockServer.takeRequest()

        val type =
            Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        val adapter = moshi.adapter<Map<String, String>>(type)
        val body = adapter.fromJson(request.body)!!

        assertTrue { request.path!!.endsWith("/15") }
        assertEquals("new name", body["name"])
    }

    @Test
    fun deleteFolderTest() = runTest {
        mockServer.enqueueOK()

        nextcloudNewsDataSource.deleteFolder(15)
        val request = mockServer.takeRequest()

        assertEquals(request.method, "DELETE")
        assertTrue { request.path!!.endsWith("/15") }
    }

    @Test
    fun setItemsReadStateTest() = runTest {
        mockServer.enqueueOK()
        mockServer.enqueueOK()

        val data = NextcloudNewsSyncData(
            readIds = listOf(15, 16, 17),
            unreadIds = listOf(18, 19, 20)
        )

        nextcloudNewsDataSource.setItemsReadState(data)
        val unreadRequest = mockServer.takeRequest()
        val readRequest = mockServer.takeRequest()

        val type =
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Types.newParameterizedType(List::class.java, Int::class.javaObjectType)
            )
        val adapter = moshi.adapter<Map<String, List<Int>>>(type)
        val unreadBody = adapter.fromJson(unreadRequest.body)!!
        val readBody = adapter.fromJson(readRequest.body)!!

        assertEquals(data.readIds, readBody["itemIds"])
        assertEquals(data.unreadIds, unreadBody["itemIds"])
    }

    @Test
    fun setItemsStarStateTest() = runTest {
        mockServer.enqueueOK()
        mockServer.enqueueOK()

        val data = NextcloudNewsSyncData(
            starredIds = listOf(15, 16, 17),
            unstarredIds = listOf(18, 19, 20)
        )

        nextcloudNewsDataSource.setItemsStarState(data)
        val starRequest = mockServer.takeRequest()
        val unstarRequest = mockServer.takeRequest()

        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Types.newParameterizedType(List::class.java, Int::class.javaObjectType)
        )
        val adapter = moshi.adapter<Map<String, List<Int>>>(type)

        val starBody = adapter.fromJson(starRequest.body)!!
        val unstarBody = adapter.fromJson(unstarRequest.body)!!

        assertEquals(data.starredIds, starBody["itemIds"])
        assertEquals(data.unstarredIds, unstarBody["itemIds"])
    }

    @Test
    fun initialSyncTest() = runTest {
        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                with(request.path!!) {
                    return when {
                        this == "/folders" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/nextcloudnews/adapters/valid_folder.json"))
                        }

                        this == "/feeds" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/nextcloudnews/adapters/feeds.json"))
                        }

                        contains("/items") -> {

                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/nextcloudnews/adapters/items.json"))
                        }

                        else -> MockResponse().setResponseCode(404)
                    }
                }
            }
        }

        val result =
            nextcloudNewsDataSource.synchronize(SyncType.INITIAL_SYNC, NextcloudNewsSyncData())

        with(result) {
            assertEquals(1, folders.size)
            assertEquals(3, feeds.size)
            assertEquals(3, items.size)
            assertEquals(3, starredItems.size)
        }
    }

    @Test
    fun classicSyncTest() = runTest {
        var setItemState = 0
        val lastModified = 10L
        val ids = listOf(1, 2, 3, 4)

        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                with(request.path!!) {
                    // important, otherwise test fails and I don't know why
                    println("request: ${request.path}")
                    return when {
                        this == "/folders" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/nextcloudnews/adapters/valid_folder.json"))
                        }

                        this == "/feeds" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/nextcloudnews/adapters/feeds.json"))
                        }

                        contains("/items/updated") -> {
                            assertEquals(
                                "$lastModified",
                                request.requestUrl!!.queryParameter("lastModified")
                            )
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/nextcloudnews/adapters/items.json"))
                        }

                        this.matches(Regex("/items/(read|unread|star|unstar)/multiple")) -> {
                            setItemState++
                            MockResponse().setResponseCode(200)
                        }

                        else -> MockResponse().setResponseCode(404)
                    }
                }
            }
        }

        val result = nextcloudNewsDataSource.synchronize(
            SyncType.CLASSIC_SYNC,
            NextcloudNewsSyncData(
                lastModified = lastModified,
                readIds = ids,
                unreadIds = ids,
                starredIds = ids,
                unstarredIds = ids
            )
        )

        with(result) {
            assertEquals(4, setItemState)
            assertEquals(1, folders.size)
            assertEquals(3, feeds.size)
            assertEquals(3, items.size)
        }
    }
}