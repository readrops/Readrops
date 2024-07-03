package com.readrops.app

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.api.services.SyncResult
import com.readrops.app.sync.SyncAnalyzer
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.test.runTest
import org.joda.time.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncAnalyzerTest {

    private lateinit var database: Database
    private lateinit var syncAnalyzer: SyncAnalyzer
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val account1 = Account(
        accountName = "test account 1",
        accountType = AccountType.FRESHRSS,
        isNotificationsEnabled = true
    )

    private val account2 = Account(
        accountName = "test account 2",
        accountType = AccountType.NEXTCLOUD_NEWS,
        isNotificationsEnabled = false
    )

    private val account3 = Account(
        accountName = "test account 3",
        accountType = AccountType.LOCAL,
        isNotificationsEnabled = true
    )

    @Before
    fun setupDb() = runTest {
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java)
            .build()

        syncAnalyzer = SyncAnalyzer(context, database)

        account1.id = database.accountDao().insert(account1).toInt()
        account2.id = database.accountDao().insert(account2).toInt()
        account3.id = database.accountDao().insert(account3).toInt()

        val accountIds = listOf(account1.id, account2.id, account3.id)
        for (i in 0..2) {
            val feed = Feed().apply {
                name = "feed ${i + 1}"
                iconUrl =
                    "https://i0.wp.com/mrmondialisation.org/wp-content/uploads/2017/05/ico_final.gif"
                this.accountId = accountIds.find { it == (i + 1) }!!
                isNotificationEnabled = i % 2 == 0
            }

            database.feedDao().insert(feed)
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testOneElementEveryWhere() = runTest {
        val item = Item(
            title = "caseOneElementEveryWhere",
            feedId = 1,
            remoteId = "item 1",
            pubDate = LocalDateTime.now()
        )

        database.itemDao().insert(item)

        val syncResult = SyncResult(items = listOf(item))
        val notificationContent = syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult))

        assertEquals("caseOneElementEveryWhere", notificationContent.content)
        assertEquals("feed 1", notificationContent.title)
        assertTrue(notificationContent.largeIcon != null)
        assertTrue(notificationContent.accountId > 0)

        database.itemDao().delete(item)
    }

    @Test
    fun testTwoItemsOneFeed() = runTest {
        val item = Item(title = "caseTwoItemsOneFeed", feedId = 1)

        val syncResult = SyncResult(items = listOf(item, item, item))
        val notificationContent = syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult))

        assertEquals(context.getString(R.string.new_items, 3), notificationContent.content)
        assertEquals("feed 1", notificationContent.title)
        assertTrue(notificationContent.largeIcon != null)
        assertTrue(notificationContent.accountId > 0)
    }

    @Test
    fun testMultipleFeeds() = runTest {
        val item = Item(feedId = 1)
        val item2 = Item(feedId = 3)

        val syncResult = SyncResult(items = listOf(item, item2))
        val notificationContent = syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult))

        assertEquals(context.getString(R.string.new_items, 2), notificationContent.content)
        assertEquals(account1.accountName, notificationContent.title)
        assertTrue(notificationContent.largeIcon != null)
        assertTrue(notificationContent.accountId > 0)
    }

    @Test
    fun testMultipleAccounts() = runTest {
        val item = Item(feedId = 1)
        val item2 = Item(feedId = 3)

        val syncResult = SyncResult(items = listOf(item, item2))
        val syncResult2 = SyncResult(items = listOf(item, item2))
        val syncResults = mapOf(account1 to syncResult, account3 to syncResult2)

        val notificationContent = syncAnalyzer.getNotificationContent(syncResults)

        assertEquals(context.getString(R.string.new_items, 4), notificationContent.title)
    }

    @Test
    fun testAccountNotificationsDisabled() = runTest {
        val item1 = Item(title = "testAccountNotificationsDisabled", feedId = 1)
        val item2 = Item(title = "testAccountNotificationsDisabled2", feedId = 1)

        val syncResult = SyncResult(items = listOf(item1, item2))
        val notificationContent = syncAnalyzer.getNotificationContent(mapOf(account2 to syncResult))

        assert(notificationContent.title == null)
        assert(notificationContent.content == null)
        assert(notificationContent.largeIcon == null)
    }

    @Test
    fun testFeedNotificationsDisabled() = runTest {
        val item1 = Item(title = "testAccountNotificationsDisabled", feedId = 2)
        val item2 = Item(title = "testAccountNotificationsDisabled2", feedId = 2)

        val syncResult = SyncResult(items = listOf(item1, item2))
        val notificationContent = syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult))

        assert(notificationContent.title == null)
        assert(notificationContent.content == null)
        assert(notificationContent.largeIcon == null)
    }

    @Test
    fun testTwoAccountsWithOneAccountNotificationsEnabled() = runTest {
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

        database.itemDao().insert(item1)

        val syncResult1 = SyncResult(items = listOf(item1))
        val syncResult2 = SyncResult(items = listOf(item2, item3))

        val syncResults = mapOf(account1 to syncResult1, account2 to syncResult2)

        val notificationContent = syncAnalyzer.getNotificationContent(syncResults)

        assertEquals("testTwoAccountsWithOneAccountNotificationsEnabled", notificationContent.content)
        assertEquals("feed 1", notificationContent.title)
        assertTrue(notificationContent.largeIcon != null)
        assertTrue(notificationContent.item != null)

        database.itemDao().delete(item1)
    }

    @Test
    fun testTwoAccountsWithOneFeedNotificationEnabled() = runTest{
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

        database.itemDao().insert(item1)

        val syncResult1 = SyncResult(items = listOf(item1))
        val syncResult2 = SyncResult(items = listOf(item2, item3))

        val syncResults = mapOf(account1 to syncResult1, account2 to syncResult2)
        val notificationContent = syncAnalyzer.getNotificationContent(syncResults)

        assertEquals("testTwoAccountsWithOneAccountNotificationsEnabled", notificationContent.content)
        assertEquals("feed 1", notificationContent.title)
        assertTrue(notificationContent.largeIcon != null)
        assertTrue(notificationContent.item != null)

        database.itemDao().delete(item1)
    }


    @Test
    fun testOneAccountTwoFeedsWithOneFeedNotificationEnabled() = runTest {
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

        database.itemDao().insert(item1)

        val syncResult = SyncResult(items = listOf(item1, item2, item3))
        val notificationContent = syncAnalyzer.getNotificationContent(mapOf(account1 to syncResult))

        assertEquals("testTwoAccountsWithOneAccountNotificationsEnabled", notificationContent.content)
        assertEquals("feed 1", notificationContent.title)
        assertTrue(notificationContent.largeIcon != null)
        assertTrue(notificationContent.item != null)
        assertTrue(notificationContent.accountId > 0)

        database.itemDao().delete(item1)
    }
}