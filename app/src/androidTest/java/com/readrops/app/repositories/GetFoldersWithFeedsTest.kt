package com.readrops.app.repositories

import com.readrops.app.testutil.ReadropsTestRule
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.ItemState
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import com.readrops.db.filters.MainFilter
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.LocalDateTime
import kotlin.test.assertNull

class GetFoldersWithFeedsTest : KoinTest {

    private val database: Database by inject()
    private val getFoldersWithFeeds: GetFoldersWithFeeds by inject()
    private val account = Account(type = AccountType.LOCAL)

    @get:Rule
    val koinTest = ReadropsTestRule()

    @Before
    fun before() = runTest {
        account.id = database.accountDao().insert(account).toInt()

        // inserting 3 folders (folder 0, folder 1, folder 2)
        repeat(3) { time ->
            database.folderDao()
                .insert(Folder(name = "Folder $time", accountId = account.id))
        }

        // inserting 2 feeds, not linked to any folder (feed 0, feed 1)
        repeat(2) { time ->
            database.feedDao().insert(Feed(name = "Feed $time", accountId = account.id))
        }

        // inserting 2 feeds linked to folder 0
        repeat(2) { time ->
            database.feedDao()
                .insert(Feed(name = "Feed ${time + 2}", folderId = 1, accountId = account.id))
        }

        // inserting 3 unread items linked to feed 0
        repeat(3) { time ->
            database.itemDao()
                .insert(
                    Item(
                        title = "Item $time",
                        feedId = 1,
                        pubDate = if (time % 2 != 0) {
                            LocalDateTime.now()
                        } else {
                            LocalDateTime.now().minusMonths(1L)
                        },
                        isStarred = time % 2 == 0
                    )
                )
        }

        // insert 3 read items items linked to feed 2
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

        // insert 4 unread items linked to feed 3
        repeat(4) { time ->
            val item = Item(
                title = "Item ${time + 3}",
                feedId = 4,
                isRead = true,
                pubDate = if (time % 2 == 0) {
                    LocalDateTime.now()
                } else {
                    LocalDateTime.now().minusMonths(1L)
                },
                remoteId = "remote/$time"
            )
            database.itemDao().insert(item)
            database.itemStateDao().insert(
                ItemState(
                    read = false,
                    starred = time % 2 == 0,
                    remoteId = "remote/$time",
                    accountId = account.id
                )
            )
        }

        // folder 0 -> (feed 2, feed 3)
        // folder 1 -> null
        // folder 2 -> null
        // null -> (feed 0, feed 1)

        // feed 0 -> 3 unread items, 2 starred items, 1 new item
        // feed 1 -> null
        // feed 2 -> 3 read items
        // feed 3 -> separate state: 4 unread items, 2 starred items, 2 new items
    }

    @Test
    fun defaultCaseTest() = runTest {
        val foldersAndFeeds = getFoldersWithFeeds.get(
            accountId = account.id,
            mainFilter = MainFilter.ALL,
            useSeparateState = false,
            hideReadFeeds = false
        ).first()

        assertEquals(4, foldersAndFeeds.size)
        assertEquals(2, foldersAndFeeds.entries.first().value.size)
        assertNull(foldersAndFeeds.entries.last().key)
        assertEquals(2, foldersAndFeeds[null]!!.size)
        assertEquals(3, foldersAndFeeds[null]!!.first().unreadCount)
    }

    @Test
    fun separateStateTest() = runTest {
        val foldersAndFeeds = getFoldersWithFeeds.get(
            accountId = account.id,
            mainFilter = MainFilter.ALL,
            useSeparateState = true,
            hideReadFeeds = false
        ).first()
        val feed = foldersAndFeeds.values.flatten().first { it.id == 4 }

        assertEquals(4, feed.unreadCount)
    }


    @Test
    fun hideReadFeedsTest() = runTest {
        val foldersAndFeeds = getFoldersWithFeeds.get(
            accountId = account.id,
            mainFilter = MainFilter.ALL,
            useSeparateState = false,
            hideReadFeeds = true
        ).first()

        assertEquals(1, foldersAndFeeds.size)
        assertNull(foldersAndFeeds.entries.first().key)
        assertEquals(1, foldersAndFeeds.entries.first().value.size)
        assertEquals(3, foldersAndFeeds.entries.first().value.first().unreadCount)
    }

    @Test
    fun hideReadFeedsWithSeparateStateTest() = runTest {
        val foldersAndFeeds = getFoldersWithFeeds.get(
            accountId = account.id,
            mainFilter = MainFilter.ALL,
            useSeparateState = true,
            hideReadFeeds = true
        ).first()

        val feed = foldersAndFeeds.values.flatten().first { it.id == 4 }

        assertEquals(1, foldersAndFeeds.size)
        assertEquals(1, foldersAndFeeds.values.flatten().size)
        assertEquals(4, feed.unreadCount)
    }

    @Test
    fun starsFilterTest() = runTest {
        val foldersAndFeeds = getFoldersWithFeeds.get(
            accountId = account.id,
            mainFilter = MainFilter.STARS,
            useSeparateState = false,
            hideReadFeeds = true
        ).first()

        assertEquals(1, foldersAndFeeds.size)
        assertEquals(2, foldersAndFeeds.values.flatten().sumOf { it.unreadCount })
    }

    @Test
    fun starsFilterSeparateStateTest() = runTest {
        val foldersAndFeeds = getFoldersWithFeeds.get(
            accountId = account.id,
            mainFilter = MainFilter.STARS,
            useSeparateState = true,
            hideReadFeeds = true
        ).first()

        assertEquals(1, foldersAndFeeds.size)
        assertEquals(2, foldersAndFeeds.values.flatten().sumOf { it.unreadCount })
    }

    @Test
    fun newFilterTest() = runTest {
        val foldersAndFeeds = getFoldersWithFeeds.get(
            accountId = account.id,
            mainFilter = MainFilter.NEW,
            useSeparateState = false,
            hideReadFeeds = true
        ).first()

        assertEquals(1, foldersAndFeeds.size)
        assertEquals(1, foldersAndFeeds.values.flatten().sumOf { it.unreadCount })
    }

    @Test
    fun newItemsUnreadCountTest() = runTest {
        val count = getFoldersWithFeeds.getNewItemsUnreadCount(1, useSeparateState = false).first()

        assertEquals(1, count)
    }

    @Test
    fun newItemsUnreadCountSeparateStateTest() = runTest {
        val count = getFoldersWithFeeds.getNewItemsUnreadCount(1, useSeparateState = true).first()

        assertEquals(2, count)
    }
}