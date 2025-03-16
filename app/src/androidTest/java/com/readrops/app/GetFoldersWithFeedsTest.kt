package com.readrops.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.readrops.app.repositories.GetFoldersWithFeeds
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import com.readrops.db.filters.MainFilter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.time.LocalDateTime
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetFoldersWithFeedsTest {

    private lateinit var database: Database
    private lateinit var getFoldersWithFeeds: GetFoldersWithFeeds
    private val account = Account(type = AccountType.LOCAL)

    @Before
    fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()

        runTest {
            account.id = database.accountDao().insert(account).toInt()

            // inserting 3 folders
            repeat(3) { time ->
                database.folderDao()
                    .insert(Folder(name = "Folder $time", accountId = account.id))
            }

            // inserting 2 feeds, not linked to any folder
            repeat(2) { time ->
                database.feedDao().insert(Feed(name = "Feed $time", accountId = account.id))
            }

            // inserting 2 feeds linked to first folder (Folder 0)
            repeat(2) { time ->
                database.feedDao()
                    .insert(Feed(name = "Feed ${time + 2}", folderId = 1, accountId = account.id))
            }

            // inserting 3 unread items linked to first feed (Feed 0)
            repeat(3) { time ->
                database.itemDao()
                    .insert(Item(title = "Item $time", feedId = 1, pubDate = LocalDateTime.now()))
            }

            // insert 3 read items items linked to second feed (feed 1)
            repeat(3) { time ->
                database.itemDao()
                    .insert(
                        Item(
                            title = "Item ${time + 3}",
                            feedId = 3,
                            isRead = true,
                            pubDate = LocalDateTime.now()
                        )
                    )
            }

            // insert 4 read items items linked to second feed (feed 1)
            repeat(4) { time ->
                val item = Item(
                    title = "Item ${time + 3}",
                    feedId = 4,
                    isRead = true,
                    pubDate = LocalDateTime.now(),
                    remoteId = "remote/$time"
                )
                database.itemDao().insert(item)
                database.itemStateDao().insert(
                    ItemState(
                        read = false,
                        remoteId = "remote/$time",
                        accountId = account.id
                    )
                )

            }
        }
    }

    @Test
    fun getFoldersWithFeedsTest() = runTest {
        getFoldersWithFeeds = GetFoldersWithFeeds(database)
        var foldersAndFeeds =
            getFoldersWithFeeds.get(account.id, MainFilter.ALL, false)
                .first()

        assertTrue { foldersAndFeeds.size == 4 }
        assertTrue { foldersAndFeeds.entries.first().value.size == 2 }
        assertTrue { foldersAndFeeds.entries.last().key == null }
        assertTrue { foldersAndFeeds[null]!!.size == 2 }
        assertTrue { foldersAndFeeds[null]!!.first().unreadCount == 3 }

        foldersAndFeeds = getFoldersWithFeeds.get(account.id, MainFilter.ALL, true).first()
        val feed = foldersAndFeeds.values.flatten().first { it.id == 4 }
        assertEquals(4, feed.unreadCount)
    }
}