package com.readrops.app

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.app.utils.SyncResultAnalyser
import com.readrops.readropsdb.Database
import com.readrops.readropsdb.entities.Feed
import com.readrops.readropsdb.entities.Item
import com.readrops.readropsdb.entities.account.Account
import com.readrops.readropsdb.entities.account.AccountType
import com.readrops.readropslibrary.services.SyncResult
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
        isNotificationsEnabled = true
    }

    @Before
    fun setupDb() {
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java)
                .build()

        var account1Id = 0
        database.accountDao().insert(account1).subscribe { id -> account1Id = id.toInt() }

        var account2Id = 0
        database.accountDao().insert(account2).subscribe { id -> account2Id = id.toInt() }

        for (i in 0..3) {
            val feed = Feed().apply {
                name = "feed ${i + 1}"
                iconUrl = "https://i0.wp.com/mrmondialisation.org/wp-content/uploads/2017/05/ico_final.gif"
                this.accountId = if (i % 2 == 0) account1Id else account2Id
                isNotificationEnabled = true
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
    }

    @Test
    fun testMultipleFeeds() {
        val item = Item().apply { feedId = 1 }
        val item2 = Item().apply { feedId = 2 }

        val syncResult = SyncResult().apply { items = listOf(item, item2) }
        val notifContent = SyncResultAnalyser(context, mapOf(Pair(account1, syncResult)), database).getSyncNotifContent()

        assertEquals(context.getString(R.string.new_items, 2), notifContent.content)
        assertEquals(account1.accountName, notifContent.title)
        assertTrue(notifContent.largeIcon != null)
    }

    @Test
    fun testMultipleAccounts() {
        val item = Item().apply { feedId = 1 }
        val item2 = Item().apply { feedId = 2 }

        val syncResult = SyncResult().apply { items = listOf(item, item2) }
        val syncResult2 = SyncResult().apply { items = listOf(item, item2) }

        val syncResults = mutableMapOf<Account, SyncResult>().apply {
            put(account1, syncResult)
            put(account2, syncResult2)
        }

        val notifContent = SyncResultAnalyser(context, syncResults, database).getSyncNotifContent()

        assertEquals("Notifications", notifContent.title)
        assertEquals(context.getString(R.string.new_items, 4), notifContent.content)
    }
}