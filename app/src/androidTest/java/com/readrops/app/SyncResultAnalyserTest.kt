package com.readrops.app

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.app.notifications.sync.SyncResultAnalyser
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import com.readrops.api.services.SyncResult
import org.joda.time.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncResultAnalyserTest {

    private lateinit var database: Database

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val account1 = Account().apply {
        accountName = "test account 1"
        accountType = AccountType.FRESHRSS
        isNotificationsEnabled = true
    }

    private val account2 = Account().apply {
        accountName = "test account 2"
        accountType = AccountType.NEXTCLOUD_NEWS
        isNotificationsEnabled = false
    }

    private val account3 = Account().apply {
        accountName = "test account 3"
        accountType = AccountType.LOCAL
        isNotificationsEnabled = true
    }

    @Before
    fun setupDb() {
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java)
                .build()

        var account1Id = 0
        database.accountDao().insert(account1).subscribe { id -> account1Id = id.toInt() }
        account1.id = account1Id

        var account2Id = 0
        database.accountDao().insert(account2).subscribe { id -> account2Id = id.toInt() }
        account2.id = account2Id

        var account3Id = 0
        database.accountDao().insert(account3).subscribe { id -> account3Id = id.toInt() }
        account3.id = account3Id

        val accountIds = listOf(account1Id, account2Id, account3Id)
        for (i in 0..2) {
            val feed = Feed().apply {
                name = "feed ${i + 1}"
                iconUrl = "https://i0.wp.com/mrmondialisation.org/wp-content/uploads/2017/05/ico_final.gif"
                this.accountId = accountIds.find { it == (i + 1) }!!
                isNotificationEnabled = i % 2 == 0
            }

            database.feedDao().insert(feed).subscribe()
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testOneElementEveryWhere() {
        val item = Item().apply {
            title = "caseOneElementEveryWhere"
            feedId = 1
            remoteId = "item 1"
            pubDate = LocalDateTime.now()
        }

        database.itemDao()
                .insert(item)
                .subscribe()

        val syncResult = SyncResult().apply { items = mutableListOf(item) }
        val notifContent = SyncResultAnalyser(context, mapOf(Pair(account1, syncResult)), database).getSyncNotifContent()

        assertEquals("caseOneElementEveryWhere", notifContent.content)
        assertEquals("feed 1", notifContent.title)
        assertTrue(notifContent.largeIcon != null)
        assertTrue(notifContent.accountId!! > 0)

        database.itemDao()
                .delete(item)
                .subscribe()
    }

    @Test
    fun testTwoItemsOneFeed() {
        val item = Item().apply {
            title = "caseTwoItemsOneFeed"
            feedId = 1
        }

        val syncResult = SyncResult().apply { items = listOf(item, item, item) }
        val notifContent = SyncResultAnalyser(context, mapOf(Pair(account1, syncResult)), database).getSyncNotifContent()

        assertEquals(context.getString(R.string.new_items, 3), notifContent.content)
        assertEquals("feed 1", notifContent.title)
        assertTrue(notifContent.largeIcon != null)
        assertTrue(notifContent.accountId!! > 0)
    }

    @Test
    fun testMultipleFeeds() {
        val item = Item().apply { feedId = 1 }
        val item2 = Item().apply { feedId = 3 }

        val syncResult = SyncResult().apply { items = listOf(item, item2) }
        val notifContent = SyncResultAnalyser(context, mapOf(Pair(account1, syncResult)), database).getSyncNotifContent()

        assertEquals(context.getString(R.string.new_items, 2), notifContent.content)
        assertEquals(account1.accountName, notifContent.title)
        assertTrue(notifContent.largeIcon != null)
        assertTrue(notifContent.accountId!! > 0)
    }

    @Test
    fun testMultipleAccounts() {
        val item = Item().apply { feedId = 1 }
        val item2 = Item().apply { feedId = 3 }

        val syncResult = SyncResult().apply { items = listOf(item, item2) }
        val syncResult2 = SyncResult().apply { items = listOf(item, item2) }

        val syncResults = mutableMapOf<Account, SyncResult>().apply {
            put(account1, syncResult)
            put(account3, syncResult2)
        }

        val notifContent = SyncResultAnalyser(context, syncResults, database).getSyncNotifContent()

        assertEquals(context.getString(R.string.new_items, 4), notifContent.title)
    }

    @Test
    fun testAccountNotificationsDisabled() {
        val item1 = Item().apply {
            title = "testAccountNotificationsDisabled"
            feedId = 1
        }

        val item2 = Item().apply {
            title = "testAccountNotificationsDisabled2"
            feedId = 1
        }

        val syncResult = SyncResult().apply { items = listOf(item1, item2) }
        val notifContent = SyncResultAnalyser(context, mapOf(Pair(account2, syncResult)), database).getSyncNotifContent()

        assert(notifContent.title == null)
        assert(notifContent.content == null)
        assert(notifContent.largeIcon == null)
    }

    @Test
    fun testFeedNotificationsDisabled() {
        val item1 = Item().apply {
            title = "testAccountNotificationsDisabled"
            feedId = 2
        }

        val item2 = Item().apply {
            title = "testAccountNotificationsDisabled2"
            feedId = 2
        }

        val syncResult = SyncResult().apply { items = listOf(item1, item2) }
        val notifContent = SyncResultAnalyser(context, mapOf(Pair(account1, syncResult)), database).getSyncNotifContent()

        assert(notifContent.title == null)
        assert(notifContent.content == null)
        assert(notifContent.largeIcon == null)
    }

    @Test
    fun testTwoAccountsWithOneAccountNotificationsEnabled() {
        val item1 = Item().apply {
            title = "testTwoAccountsWithOneAccountNotificationsEnabled"
            feedId = 1
            remoteId = "remoteId 1"
            pubDate = LocalDateTime.now()
        }

        val item2 = Item().apply {
            title = "testTwoAccountsWithOneAccountNotificationsEnabled2"
            feedId = 3
        }

        val item3 = Item().apply {
            title = "testTwoAccountsWithOneAccountNotificationsEnabled3"
            feedId = 3
        }

        database.itemDao().insert(item1).subscribe()

        val syncResult1 = SyncResult().apply { items = listOf(item1) }
        val syncResult2 = SyncResult().apply { items = listOf(item2, item3) }

        val syncResults = mutableMapOf<Account, SyncResult>().apply {
            put(account1, syncResult1)
            put(account2, syncResult2)
        }

        val notifContent = SyncResultAnalyser(context, syncResults, database).getSyncNotifContent()

        assertEquals("testTwoAccountsWithOneAccountNotificationsEnabled", notifContent.content)
        assertEquals("feed 1", notifContent.title)
        assertTrue(notifContent.largeIcon != null)
        assertTrue(notifContent.item != null)

        database.itemDao().delete(item1).subscribe()
    }

    @Test
    fun testTwoAccountsWithOneFeedNotificationEnabled() {
        val item1 = Item().apply {
            title = "testTwoAccountsWithOneAccountNotificationsEnabled"
            feedId = 1
            remoteId = "remoteId 1"
            pubDate = LocalDateTime.now()
        }

        val item2 = Item().apply {
            title = "testTwoAccountsWithOneAccountNotificationsEnabled2"
            feedId = 2
        }

        val item3 = Item().apply {
            title = "testTwoAccountsWithOneAccountNotificationsEnabled3"
            feedId = 2
        }

        database.itemDao().insert(item1).subscribe()

        val syncResult1 = SyncResult().apply { items = listOf(item1) }
        val syncResult2 = SyncResult().apply { items = listOf(item2, item3) }

        val syncResults = mutableMapOf<Account, SyncResult>().apply {
            put(account1, syncResult1)
            put(account2, syncResult2)
        }

        val notifContent = SyncResultAnalyser(context, syncResults, database).getSyncNotifContent()

        assertEquals("testTwoAccountsWithOneAccountNotificationsEnabled", notifContent.content)
        assertEquals("feed 1", notifContent.title)
        assertTrue(notifContent.largeIcon != null)
        assertTrue(notifContent.item != null)

        database.itemDao().delete(item1).subscribe()
    }


    @Test
    fun testOneAccountTwoFeedsWithOneFeedNotificationEnabled() {
        val item1 = Item().apply {
            title = "testTwoAccountsWithOneAccountNotificationsEnabled"
            feedId = 1
            remoteId = "remoteId 1"
            pubDate = LocalDateTime.now()
        }

        val item2 = Item().apply {
            title = "testTwoAccountsWithOneAccountNotificationsEnabled2"
            feedId = 2
        }

        val item3 = Item().apply {
            title = "testTwoAccountsWithOneAccountNotificationsEnabled3"
            feedId = 2
        }

        database.itemDao().insert(item1).subscribe()

        val syncResult = SyncResult().apply { items = listOf(item1, item2, item3) }
        val notifContent = SyncResultAnalyser(context, mapOf(Pair(account1, syncResult)), database).getSyncNotifContent()

        assertEquals("testTwoAccountsWithOneAccountNotificationsEnabled", notifContent.content)
        assertEquals("feed 1", notifContent.title)
        assertTrue(notifContent.largeIcon != null)
        assertTrue(notifContent.item != null)
        assertTrue(notifContent.accountId!! > 0)

        database.itemDao().delete(item1).subscribe()
    }
}