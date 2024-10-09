package com.readrops.api.services.fever

import com.readrops.api.TestUtils
import com.readrops.api.apiModule
import com.readrops.api.enqueueOK
import com.readrops.api.enqueueOKStream
import com.readrops.api.okResponseWithBody
import com.readrops.api.services.SyncType
import com.readrops.api.utils.AuthInterceptor
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeverDataSourceTest : KoinTest {

    private lateinit var dataSource: FeverDataSource
    private val mockServer = MockWebServer()

    @Before
    fun before() {
        mockServer.start(8080)
        val url = mockServer.url("")
        dataSource = get(parameters = {
            parametersOf(FeverCredentials(null, null, url.toString()))
        })
    }

    @After
    fun tearDown() {
        mockServer.close()
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(apiModule, module {
            single {
                OkHttpClient.Builder()
                    .callTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.HOURS)
                    .addInterceptor(get<AuthInterceptor>())
                    .build()
            }

        })
    }

    @Test
    fun loginSuccessfulTest() = runTest {
        val stream = TestUtils.loadResource("services/fever/successful_auth.json")
        mockServer.enqueueOKStream(stream)

        assertTrue { dataSource.login("", "") }
    }

    @Test
    fun loginFailedTest() = runTest {
        val stream = TestUtils.loadResource("services/fever/failed_auth.json")
        mockServer.enqueueOKStream(stream)

        assertFalse { dataSource.login("", "") }
    }

    @Test
    fun setItemStateTest() = runTest {
        mockServer.enqueueOK()

        dataSource.setItemState("login", "password", "saved", "itemId")
        val request = mockServer.takeRequest()
        val requestBody = request.body.readUtf8()

        assertEquals("saved", request.requestUrl?.queryParameter("as"))
        assertEquals("itemId", request.requestUrl?.queryParameter("id"))

        assertTrue { requestBody.contains("api_key") }
        assertTrue { requestBody.contains("fb2f5a9b0eccc1ee95c1d559a2dd797a") }
    }

    @Test
    fun initialSyncTest() = runTest {
        var pageNumber = 0
        var firstMaxId = ""
        var secondMaxId = ""
        var thirdMaxId = ""

        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                with(request.path!!) {
                    return when {
                        this == "/?feeds" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/feeds.json"))
                        }

                        this == "/?groups" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/folders.json"))
                        }

                        this == "/?favicons" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/favicons.json"))
                        }

                        this == "/?unread_item_ids" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/itemsIds.json"))
                        }

                        this == "/?saved_item_ids" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/itemsIds.json"))
                        }

                        contains("/?items") -> {
                            when (pageNumber++) {
                                0 -> {
                                    firstMaxId = request.requestUrl?.queryParameter("max_id").orEmpty()
                                    MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/items_page2.json"))
                                }
                                1 -> {
                                    secondMaxId = request.requestUrl?.queryParameter("max_id").orEmpty()
                                    MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/items_page1.json"))
                                }
                                2 -> {
                                    thirdMaxId = request.requestUrl?.queryParameter("max_id").orEmpty()
                                    MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/empty_items.json"))
                                }
                                else -> MockResponse().setResponseCode(404)
                            }
                        }

                        else -> MockResponse().setResponseCode(404)
                    }

                }
            }
        }

        val result = dataSource.synchronize("login", "password", SyncType.INITIAL_SYNC, "")

        assertEquals(1, result.folders.size)
        assertEquals(1, result.feverFeeds.feeds.size)
        assertEquals(3, result.favicons.size)
        assertEquals(6, result.unreadIds.size)
        assertEquals(6, result.starredIds.size)
        assertEquals(10, result.items.size)
        assertEquals(10, result.items.size)
        assertEquals(1564058340320135, result.sinceId)

        assertEquals("1564058340320135", firstMaxId)
        assertEquals("6", secondMaxId)
        assertEquals("1", thirdMaxId)
    }

    @Test
    fun classicSyncTest() = runTest {
        var pageNumber = 0

        var firstLastSinceId = ""
        var secondLastSinceId = ""
        var thirdLastSinceId = ""

        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                with(request.path!!) {
                    return when {
                        this == "/?feeds" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/feeds.json"))
                        }

                        this == "/?groups" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/folders.json"))
                        }

                        this == "/?favicons" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/favicons.json"))
                        }

                        this == "/?unread_item_ids" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/itemsIds.json"))
                        }

                        this == "/?saved_item_ids" -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/itemsIds.json"))
                        }

                        contains("/?items") -> {
                            when (pageNumber++) {
                                0 -> {
                                    firstLastSinceId = request.requestUrl?.queryParameter("since_id").orEmpty()
                                    MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/items_page1.json"))
                                }
                                1 -> {
                                    secondLastSinceId = request.requestUrl?.queryParameter("since_id").orEmpty()
                                    MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/items_page2.json"))
                                }
                                2 -> {
                                    thirdLastSinceId = request.requestUrl?.queryParameter("since_id").orEmpty()
                                    MockResponse.okResponseWithBody(TestUtils.loadResource("services/fever/empty_items.json"))
                                }
                                else -> MockResponse().setResponseCode(404)
                            }
                        }

                        else -> MockResponse().setResponseCode(404)
                    }

                }
            }
        }

        val result = dataSource.synchronize("login", "password", SyncType.CLASSIC_SYNC, "1")

        assertEquals(1, result.folders.size)
        assertEquals(1, result.feverFeeds.feeds.size)
        assertEquals(3, result.favicons.size)
        assertEquals(6, result.unreadIds.size)
        assertEquals(6, result.starredIds.size)
        assertEquals(10, result.items.size)
        assertEquals(10, result.sinceId)

        assertEquals("1", firstLastSinceId)
        assertEquals("5", secondLastSinceId)
        assertEquals("10", thirdLastSinceId)

        mockServer.dispatcher.shutdown()
    }
}