package com.readrops.app.sync

import android.content.Context
import android.util.Log
import com.readrops.app.R
import com.readrops.app.repositories.SyncResult
import com.readrops.app.testutil.ReadropsTestRule
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import java.time.LocalDateTime
import kotlin.test.assertNotNull

class SyncAnalyzerTest : KoinTest {

    private val database: Database by inject()
    private val syncAnalyzer: SyncAnalyzer by inject()

    @get:Rule
    val testRule = ReadropsTestRule()

    private val account1 = Account(
        name = "test account 1",
        type = AccountType.FRESHRSS,
        isNotificationsEnabled = true
    )

    private val account2 = Account(
        name = "test account 2",
        type = AccountType.NEXTCLOUD_NEWS,
        isNotificationsEnabled = false
    )

    private val account3 = Account(
        name = "test account 3",
        type = AccountType.LOCAL,
        isNotificationsEnabled = true
    )

    @Before
    fun before() = runTest {
        println("BeforeAll called")
        val accounts = listOf(
            account1,
            account2,
            account3
        )

        database.accountDao().insert(accounts)
            .zip(accounts)
            .forEach { (id, account) -> account.id = id.toInt() }

        for ((index, account) in accounts.withIndex()) {
            val feed = Feed(
                name = "Feed $index",
                iconUrl = "https://url.com/icon.jpg",
                accountId = account.id,
                isNotificationEnabled = index % 2 == 0,
            )

            database.feedDao().insert(feed)
        }
    }

    @Test
    fun oneElementEveryWhereTest() = runTest {
        val item = Item(
            title = "caseOneElementEveryWhere",
            feedId = 1,
            remoteId = "item 1",
            pubDate = LocalDateTime.now()
        )

        val syncResult = SyncResult(items = listOf(item))
        val content = syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult))

        assertNotNull(content)
        assertEquals("caseOneElementEveryWhere", content.text)
        assertEquals("Feed 0", content.title)
        assertTrue(content.largeIcon != null)
        assertTrue(content.accountId > 0)
    }

    @Test
    fun twoItemsOneFeedTest() = runTest {
        val item = Item(title = "caseTwoItemsOneFeed", feedId = 1)
        val syncResult = SyncResult(items = listOf(item, item, item))

        syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult)).let { content ->
            assertNotNull(content)

            assertEquals(get<Context>().getString(R.string.new_items, 3), content.text)
            assertEquals("Feed 0", content.title)
            assertTrue(content.largeIcon != null)
            assertTrue(content.accountId > 0)
        }
    }

    @Test
    fun multipleFeedsTest() = runTest {
        val item = Item(feedId = 1)
        val item2 = Item(feedId = 3)

        val syncResult = SyncResult(items = listOf(item, item2))
        val content = syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult))

        assertNotNull(content)
        assertEquals(get<Context>().getString(R.string.new_items, 2), content.text)
        assertEquals(account1.name, content.title)
        assertTrue(content.largeIcon != null)
        assertTrue(content.accountId > 0)
    }

    @Test
    fun multipleAccountsTest() = runTest {
        val item = Item(feedId = 1)
        val item2 = Item(feedId = 3)

        val syncResult = SyncResult(items = listOf(item, item2))
        val syncResult2 = SyncResult(items = listOf(item, item2))
        val syncResults = mapOf(account1 to syncResult, account3 to syncResult2)

        val content = syncAnalyzer.getNotificationContent(syncResults)

        assertNotNull(content)
        assertEquals(get<Context>().getString(R.string.new_items, 4), content.title)
    }

    @Test
    fun accountNotificationsDisabledTest() = runTest {
        val item1 = Item(title = "testAccountNotificationsDisabled", feedId = 1)
        val item2 = Item(title = "testAccountNotificationsDisabled2", feedId = 1)

        val syncResult = SyncResult(items = listOf(item1, item2))
        assertNull(syncAnalyzer.getNotificationContent(mapOf(account2 to syncResult)))
    }

    @Test
    fun feedNotificationsDisabledTest() = runTest {
        val item1 = Item(title = "testAccountNotificationsDisabled", feedId = 2)
        val item2 = Item(title = "testAccountNotificationsDisabled2", feedId = 2)

        Log.d("SyncAnalyzerTest", "$account1")

        val syncResult = SyncResult(items = listOf(item1, item2))
        val content = syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult))
        assertNull(content)
    }

    @Test
    fun twoAccountsWithOneAccountNotificationsEnabledTest() = runTest {
        val item1 = Item(
            title = "testTwoAccountsWithOneAccountNotificationsEnabled",
            feedId = 1,
            remoteId = "remoteId 1",
            pubDate = LocalDateTime.now()
        )

        val item2 = Item(
            title = "testTwoAccountsWithOneAccountNotificationsEnabled2",
            feedId = 3
        )

        val item3 = Item(
            title = "testTwoAccountsWithOneAccountNotificationsEnabled3",
            feedId = 3
        )

        val syncResult1 = SyncResult(items = listOf(item1))
        val syncResult2 = SyncResult(items = listOf(item2, item3))

        val syncResults = mapOf(account1 to syncResult1, account2 to syncResult2)

        val content = syncAnalyzer.getNotificationContent(syncResults)

        assertNotNull(content)
        assertEquals("testTwoAccountsWithOneAccountNotificationsEnabled", content.text)
        assertEquals("Feed 0", content.title)
        assertTrue(content.largeIcon != null)
        assertTrue(content.item != null)
    }

    @Test
    fun twoAccountsWithOneFeedNotificationEnabledTest() = runTest {
        val item1 = Item(
            title = "testTwoAccountsWithOneAccountNotificationsEnabled",
            feedId = 1,
            remoteId = "remoteId 1",
            pubDate = LocalDateTime.now()
        )

        val item2 = Item(
            title = "testTwoAccountsWithOneAccountNotificationsEnabled2",
            feedId = 2
        )

        val item3 = Item(
            title = "testTwoAccountsWithOneAccountNotificationsEnabled3",
            feedId = 2
        )

        val syncResult1 = SyncResult(items = listOf(item1))
        val syncResult2 = SyncResult(items = listOf(item2, item3))

        val syncResults = mapOf(account1 to syncResult1, account2 to syncResult2)

        val content = syncAnalyzer.getNotificationContent(syncResults)

        assertNotNull(content)
        assertEquals("testTwoAccountsWithOneAccountNotificationsEnabled", content.text)
        assertEquals("Feed 0", content.title)
        assertTrue(content.largeIcon != null)
        assertTrue(content.item != null)
    }


    @Test
    fun oneAccountTwoFeedsWithOneFeedNotificationEnabledTest() = runTest {
        val item1 = Item(
            title = "testTwoAccountsWithOneAccountNotificationsEnabled",
            feedId = 1,
            remoteId = "remoteId 1",
            pubDate = LocalDateTime.now()
        )

        val item2 = Item(
            title = "testTwoAccountsWithOneAccountNotificationsEnabled2",
            feedId = 2
        )

        val item3 = Item(
            title = "testTwoAccountsWithOneAccountNotificationsEnabled3",
            feedId = 2
        )

        val syncResult = SyncResult(items = listOf(item1, item2, item3))
        val content = syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult))

        assertNotNull(content)
        assertEquals("testTwoAccountsWithOneAccountNotificationsEnabled", content.text)
        assertEquals("Feed 0", content.title)
        assertTrue(content.largeIcon != null)
        assertTrue(content.item != null)
        assertTrue(content.accountId > 0)
    }
}