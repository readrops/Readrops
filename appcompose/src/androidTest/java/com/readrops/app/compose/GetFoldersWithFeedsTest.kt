package com.readrops.app.compose

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.joda.time.LocalDateTime
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class GetFoldersWithFeedsTest {

    private lateinit var database: Database
    private lateinit var getFoldersWithFeeds: GetFoldersWithFeeds
    private val account = Account(accountType = AccountType.LOCAL)

    @Before
    fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()

        runTest {
            account.id = database.newAccountDao().insert(account).toInt()

            repeat(3) { time ->
                database.newFolderDao().insert(Folder(name = "Folder $time", accountId = account.id))
            }

            repeat(2) { time ->
                database.newFeedDao().insert(Feed(name = "Feed $time", accountId = account.id))
            }

            repeat(2) { time ->
                database.newFeedDao().insert(Feed(name = "Feed ${time+2}", folderId = 1, accountId = account.id))
            }

            repeat(3) { time ->
                database.newItemDao().insert(Item(title = "Item $time", feedId = 1, pubDate = LocalDateTime.now()))
            }
        }
    }

    @Test
    fun getFoldersWithFeedsTest() = runTest {
        getFoldersWithFeeds = GetFoldersWithFeeds(database, StandardTestDispatcher(testScheduler))
        val foldersAndFeeds = getFoldersWithFeeds.get(account.id)

        assertTrue { foldersAndFeeds.size == 4 }
        assertTrue { foldersAndFeeds.entries.first().value.size == 2 }
        assertTrue { foldersAndFeeds.entries.last().key == null }
        assertTrue { foldersAndFeeds[null]!!.size == 2 }
        assertTrue { foldersAndFeeds[null]!!.first().unreadCount == 3 }

    }
}