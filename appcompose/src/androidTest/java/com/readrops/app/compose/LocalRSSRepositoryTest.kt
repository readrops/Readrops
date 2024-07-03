package com.readrops.app.compose

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.readrops.api.apiModule
import com.readrops.api.utils.ApiUtils
import com.readrops.api.utils.AuthInterceptor
import com.readrops.app.compose.repositories.LocalRSSRepository
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Before
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class LocalRSSRepositoryTest : KoinTest {

    private val mockServer: MockWebServer = MockWebServer()
    private val account = Account(accountType = AccountType.LOCAL)
    private lateinit var database: Database
    private lateinit var repository: LocalRSSRepository
    private lateinit var feeds: List<Feed>

    @Before
    fun before() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()

        KoinTestRule.create {
            modules(apiModule, module {
                single { database }
                single {
                    OkHttpClient.Builder()
                        .callTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(1, TimeUnit.HOURS)
                        .addInterceptor(get<AuthInterceptor>())
                        .build()
                }
            })
        }

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

        repository = LocalRSSRepository(get(), database, account)
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
            database.itemDao().itemExists(result.first.items.first().guid!!, account.id)
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
            database.itemDao().itemExists(result.first.items.first().guid!!, account.id)
        }
    }
}