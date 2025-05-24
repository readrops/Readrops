package com.readrops.app.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat.Builder
import androidx.test.core.app.ApplicationProvider
import coil3.imageLoader
import com.readrops.api.utils.ApiUtils
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.app.testutil.ReadropsTestRule
import com.readrops.app.testutil.TestUtils
import com.readrops.app.testutil.okResponseWithBody
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject
import java.net.HttpURLConnection

class SynchronizerTest : KoinTest {

    // TODO database.accountDao().selectAllAccounts().first() test case
    // TODO FeedColors.getFeedColor in fetchFeedColors test case, but should wait for FeedColors.getFeedColor coil usage?

    private val database: Database by inject()
    private val encryptedSharedPreferences: SharedPreferences by inject()
    private val synchronizer: Synchronizer by inject()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val mockServer = MockWebServer()

    @get:Rule
    val rule = ReadropsTestRule()

    private val remoteAccount = Account(
        name = "Remote account",
        type = AccountType.FRESHRSS,
        url = mockServer.url("/remote").toString(),
        writeToken = "writeToken"
    )

    private val localAccount = Account(
        name = "Local account",
        type = AccountType.LOCAL
    )

    private val feverAccount = Account(
        name = "Fever account",
        type = AccountType.FEVER,
        url = mockServer.url("/fever/").toString(),
        login = "login",
        password = "password"
    )

    private val localFeed = Feed(
        name = "Hacker News",
        url = mockServer.url("/local").toString()
    )

    private val localFolder = Folder(
        name = "Local folder"
    )

    @Before
    fun before() = runTest {
        //mockServer.start()

        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/rss+xml")
                    .setBody(Buffer().readFrom(TestUtils.loadResource("rss_feed.xml")))
            }
        }

        remoteAccount.id = database.accountDao().insert(remoteAccount).toInt()
        localAccount.id = database.accountDao().insert(localAccount).toInt()
        feverAccount.id = database.accountDao().insert(feverAccount).toInt()

        localFolder.apply {
            accountId = localAccount.id
            id = database.folderDao().insert(localFolder).toInt()
        }

        localFeed.apply {
            accountId = localAccount.id
            folderId = localFolder.id
            id = database.feedDao().insert(this).toInt()
        }

        encryptedSharedPreferences.edit()
            .putString(feverAccount.loginKey, feverAccount.login)
            .putString(feverAccount.passwordKey, feverAccount.password)
            .commit()
    }

    @After
    fun after() {
        mockServer.shutdown()
        database.clearAllTables()
        // important when working with cached favicons
        context.cacheDir.resolve("image_cache").deleteRecursively()
    }

    @Test
    fun localAccountTest() = runTest {
        val notificationBuilder = Builder(context, ReadropsApp.SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)

        synchronizer.synchronizeAccounts(
            notificationBuilder,
            SyncInputData(localAccount.id, -1, -1)
        ) { feed, feedMax, feedCount ->
            assertEquals("Hacker News", feed.name)
            assertEquals(1, feedMax)
            assertEquals(1, feedCount)
        }

        val items = database.itemDao().selectItems(localFeed.id)
        assertEquals(7, items.size)
    }

    @Test
    fun localAccountFeedSelectionTest() = runTest {
        val notificationBuilder = Builder(context, ReadropsApp.SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)

        synchronizer.synchronizeAccounts(
            notificationBuilder,
            SyncInputData(localAccount.id, localFeed.id, -1)
        ) { feed, feedMax, feedCount ->
            assertEquals("Hacker News", feed.name)
            assertEquals(1, feedMax)
            assertEquals(1, feedCount)
        }

        val items = database.itemDao().selectItems(localFeed.id)
        assertEquals(7, items.size)
    }

    @Test
    fun localAccountFolderSelectionTest() = runTest {
        val notificationBuilder = Builder(context, ReadropsApp.SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)

        synchronizer.synchronizeAccounts(
            notificationBuilder,
            SyncInputData(localAccount.id, -1, localFolder.id)
        ) { feed, feedMax, feedCount ->
            assertEquals("Hacker News", feed.name)
            assertEquals(1, feedMax)
            assertEquals(1, feedCount)
        }

        val items = database.itemDao().selectItems(localFeed.id)
        assertEquals(7, items.size)
    }


    @Test
    fun remoteAccountTest() = runTest {
        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                with(request.path!!) {
                    return when {
                        contains("tag/list") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("greader/folders.json"))
                        }

                        contains("subscription/list") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("greader/feeds.json"))
                        }

                        // items
                        contains("contents/user/-/state/com.google/reading-list") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("greader/items.json"))
                        }

                        // starred items
                        contains("contents/user/-/state/com.google/starred") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("greader/items.json"))
                        }

                        // unread ids & starred ids
                        contains("stream/items/ids") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("greader/items_starred_ids.json"))
                        }

                        else -> MockResponse().setResponseCode(404)
                    }
                }
            }

        }

        val notificationBuilder = Builder(context, ReadropsApp.SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)

        synchronizer.synchronizeAccounts(
            notificationBuilder,
            SyncInputData(remoteAccount.id, -1, -1)
        ) { feed, feedMax, feedCount ->

        }

        val feeds = database.feedDao().selectFeeds(remoteAccount.id)
        assertEquals(1, feeds.size)

        // contains both unstarred and starred items
        val items = database.itemDao().selectItems(feeds.first().id)
        assertEquals(4, items.size)
    }


    @Test
    fun feverFaviconsTest() = runTest {
        mockServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                with(request.path!!) {
                    return when {
                        contains("/?feeds") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("fever/feeds.json"))
                        }

                        contains("/?groups") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("fever/folders.json"))
                        }

                        contains("/?favicons") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("fever/favicons.json"))
                        }

                        contains("/?unread_item_ids") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("fever/itemsIds.json"))
                        }

                        contains("/?saved_item_ids") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("fever/itemsIds.json"))
                        }

                        contains("/?items") -> {
                            MockResponse.okResponseWithBody(TestUtils.loadResource("fever/items_page1.json"))
                        }

                        else -> MockResponse().setResponseCode(404)
                    }
                }
            }

        }

        val notificationBuilder = Builder(context, ReadropsApp.SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)

        synchronizer.synchronizeAccounts(
            notificationBuilder,
            SyncInputData(feverAccount.id, -1, -1)
        ) { feed, feedMax, feedCount ->

        }

        val feeds = database.feedDao().selectFeeds(feverAccount.id)
        val diskCache = context.imageLoader.diskCache!!

        assertNotNull { diskCache.openSnapshot(feeds.first().iconUrl!!) }
    }
}