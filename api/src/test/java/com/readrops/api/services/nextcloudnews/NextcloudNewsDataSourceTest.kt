package com.readrops.api.services.nextcloudnews

import com.readrops.api.TestUtils
import com.readrops.api.apiModule
import com.readrops.api.enqueueOK
import com.readrops.api.enqueueStream
import com.readrops.api.services.nextcloudnews.NextNewsDataSource.ItemQueryType
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.StarItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
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

    private lateinit var nextcloudNewsDataSource: NewNextcloudNewsDataSource
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
                    .create(NewNextcloudNewsService::class.java)
            }
        })
    }

    @Before
    fun before() {
        mockServer.start(8080)
        nextcloudNewsDataSource = NewNextcloudNewsDataSource(get())
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun loginTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/user.xml")
        val account = Account(login = "login", url = mockServer.url("").toString())

        mockServer.enqueueStream(stream)

        val displayName = nextcloudNewsDataSource.login(get(), account)
        val request = mockServer.takeRequest()

        assertTrue { displayName == "Shinokuni" }
        assertTrue { request.headers.contains("OCS-APIRequest" to "true") }
        assertTrue { request.path == "//ocs/v1.php/cloud/users/login" }
    }

    @Test
    fun foldersTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/valid_folder.json")
        mockServer.enqueueStream(stream)

        val folders = nextcloudNewsDataSource.getFolders()
        assertTrue { folders.size == 1 }
    }

    @Test
    fun feedsTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/feeds.json")
        mockServer.enqueueStream(stream)

        val feeds = nextcloudNewsDataSource.getFeeds()
        assertTrue { feeds.size == 3 }
    }

    @Test
    fun itemsTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/items.json")
        mockServer.enqueueStream(stream)

        val items = nextcloudNewsDataSource.getItems(ItemQueryType.ALL.value, false, 10)
        val request = mockServer.takeRequest()

        assertTrue { items.size == 3 }
        with(request.requestUrl!!) {
            assertEquals("3", queryParameter("type"))
            assertEquals("false", queryParameter("getRead"))
            assertEquals("10", queryParameter("batchSize"))
        }
    }

    @Test
    fun newItemsTest() = runTest {
        val stream = TestUtils.loadResource("services/nextcloudnews/adapters/items.json")
        mockServer.enqueueStream(stream)

        val items =
            nextcloudNewsDataSource.getNewItems(1512, NewNextcloudNewsDataSource.ItemQueryType.ALL)
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
        mockServer.enqueueStream(stream)

        val feeds = nextcloudNewsDataSource.createFeed("https://news.ycombinator.com/rss", null)
        val request = mockServer.takeRequest()

        assertTrue { feeds.isNotEmpty() }
        with(request.requestUrl!!) {
            assertEquals("https://news.ycombinator.com/rss", queryParameter("url"))
            assertEquals(null, queryParameter("folderId"))
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

        val type =
            Types.newParameterizedType(
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
        mockServer.enqueueStream(stream)

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

        assertEquals(data.readIds, readBody["items"])
        assertEquals(data.unreadIds, unreadBody["items"])
    }

    @Test
    fun setItemsStarStateTest() = runTest {
        mockServer.enqueueOK()
        mockServer.enqueueOK()

        val starList = listOf(
            StarItem("remote1", "guid1"),
            StarItem("remote2", "guid2")
        )
        nextcloudNewsDataSource.setItemsStarState(
            NewNextcloudNewsDataSource.StateType.STAR,
            starList
        )

        val starRequest = mockServer.takeRequest()

        val unstarList = listOf(
            StarItem("remote3", "guid3"),
            StarItem("remote4", "guid4")
        )
        nextcloudNewsDataSource.setItemsStarState(
            NewNextcloudNewsDataSource.StateType.UNSTAR,
            unstarList
        )

        val unstarRequest = mockServer.takeRequest()

        val type =
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Types.newParameterizedType(
                    List::class.java,
                    Types.newParameterizedType(
                        Map::class.java,
                        String::class.java,
                        String::class.java
                    )
                )
            )
        val adapter = moshi.adapter<Map<String, List<Map<String, String>>>>(type)

        val starBody = adapter.fromJson(starRequest.body)!!
        val unstarBody = adapter.fromJson(unstarRequest.body)!!

        assertEquals(starList[0].feedRemoteId, starBody.values.first().first()["feedId"])
        assertEquals(unstarList[0].feedRemoteId, unstarBody.values.first().first()["feedId"])
    }
}