package com.readrops.app.repositories

import com.readrops.api.utils.ApiUtils
import com.readrops.app.testutil.ReadropsTestRule
import com.readrops.app.testutil.TestUtils
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import java.net.HttpURLConnection
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalRSSRepositoryTest : KoinTest {

    private val mockServer = MockWebServer()
    private val database: Database by inject()

    private val account = Account(type = AccountType.LOCAL)
    private lateinit var repository: LocalRSSRepository
    private lateinit var feeds: List<Feed>

    @get:Rule
    val koinTest = ReadropsTestRule()

    @Before
    fun before() = runTest {
        mockServer.start()
        val url = mockServer.url("/rss")

        account.id = database.accountDao().insert(account).toInt()
        feeds = listOf(
            Feed(
                name = "feedTest",
                url = url.toString(),
                accountId = account.id,
            ),
        )

        database.feedDao().insert(feeds).apply {
            feeds.first().id = first().toInt()
        }

        repository = get<BaseRepository> { parametersOf(account) } as LocalRSSRepository
    }

    @After
    fun after() {
        mockServer.shutdown()
    }

    @Test
    fun synchronizeTest() = runTest {
        val stream = TestUtils.loadResource("rss_feed.xml")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/xml; charset=UTF-8")
                .setBody(Buffer().readFrom(stream))
        )

        val result = repository.synchronize(listOf()) {
            assertEquals(it.name, feeds.first().name)
        }

        assertTrue { result.first.items.isNotEmpty() }
        assertTrue {
            database.itemDao().itemExists(result.first.items.first().remoteId!!, account.id)
        }
    }

    @Test
    fun synchronizeWithFeedsTest(): Unit = runBlocking {
        val stream = TestUtils.loadResource("rss_feed.xml")

        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/xml; charset=UTF-8")
                .setBody(Buffer().readFrom(stream))
        )

        val result = repository.synchronize(feeds) {
            assertEquals(it.name, feeds.first().name)
        }

        assertTrue { result.first.items.isNotEmpty() }
        assertTrue {
            database.itemDao().itemExists(result.first.items.first().remoteId!!, account.id)
        }
    }
}