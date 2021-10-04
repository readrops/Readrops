package com.readrops.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.readrops.db.filters.FilterType
import com.readrops.db.filters.ListSortType
import com.readrops.db.queries.ItemsQueryBuilder
import com.readrops.db.queries.QueryFilters
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemsQueryBuilderTest {

    private lateinit var database: Database

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun noFilterDefaultSortCaseTest() {
        val queryFilters = QueryFilters(accountId = 1)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)

        database.query(query)

        with(query.sql) {
            assertTrue(contains("Feed.account_id = 1"))
            assertTrue(contains("read_it_later = 0"))
            assertTrue(contains("pub_date DESC"))

            assertFalse(contains("read = 0 And"))
        }

    }

    @Test
    fun feedFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, filterType = FilterType.FEED_FILTER,
                filterFeedId = 15)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        assertTrue(query.sql.contains("feed_id = 15 And read_it_later = 0"))
    }

    @Test
    fun readLaterFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, filterType = FilterType.READ_IT_LATER_FILTER)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        assertTrue(query.sql.contains("read_it_later = 1"))
    }

    @Test
    fun starsFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, filterType = FilterType.STARS_FILTER)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        assertTrue(query.sql.contains("starred = 1 And read_it_later = 0"))
    }

    @Test
    fun folderFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, filterType = FilterType.FOLDER_FILER, filterFolderId = 1)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        assertTrue(query.sql.contains("folder_id = ${queryFilters.filterFolderId} And read_it_later = 0"))
    }

    @Test
    fun oldestSortCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, sortType = ListSortType.OLDEST_TO_NEWEST,
                showReadItems = false)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        with(query.sql) {
            assertTrue(contains("read = 0 And "))
            assertTrue(contains("pub_date ASC"))
        }

    }

    @Test
    fun separateStateTest() {
        val queryFilters = QueryFilters(accountId = 1, showReadItems = false, filterType = FilterType.STARS_FILTER)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters, true)
        database.query(query)

        with(query.sql) {
            assertFalse(contains("read, starred"))
            assertTrue(contains("ItemState.read = 0 And "))
            assertTrue(contains("ItemState.starred = 1 And read_it_later = 0"))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun accountIdExceptionTest() {
        val queryFilters = QueryFilters()
        ItemsQueryBuilder.buildItemsQuery(queryFilters)
    }

    @Test(expected = IllegalArgumentException::class)
    fun filterFeedIdExceptionTest() {
        val queryFilters = QueryFilters(accountId = 1, filterType = FilterType.FEED_FILTER)
        ItemsQueryBuilder.buildItemsQuery(queryFilters)
    }
}