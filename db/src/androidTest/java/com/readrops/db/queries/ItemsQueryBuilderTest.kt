package com.readrops.db.queries

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.readrops.db.Database
import com.readrops.db.filters.MainFilter
import com.readrops.db.filters.OrderField
import com.readrops.db.filters.OrderType
import com.readrops.db.filters.QueryFilters
import com.readrops.db.filters.SubFilter
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Assert.assertThrows
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
            assertTrue(contains("pub_date DESC"))

            assertFalse(contains("read = 0 And"))
        }

    }

    @Test
    fun feedFilterCaseTest() {
        val queryFilters = QueryFilters(
            accountId = 1,
            subFilter = SubFilter.FEED,
            feedId = 15
        )

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        assertTrue(query.sql.contains("feed_id = 15"))
    }

    @Test
    fun starsFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, mainFilter = MainFilter.STARS)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        assertTrue(query.sql.contains("starred = 1"))
    }

    @Test
    fun newFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, mainFilter = MainFilter.NEW)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        assertTrue(query.sql.contains("Between DateTime(DateTime(\"now\"), \"-24 hour\") And DateTime(\"now\")"))
    }

    @Test
    fun folderFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, subFilter = SubFilter.FOLDER, folderId = 1)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        assertTrue(query.sql.contains("folder_id = ${queryFilters.folderId}"))
    }

    @Test
    fun oldestSortCaseTest() {
        val queryFilters = QueryFilters(
            accountId = 1,
            orderType = OrderType.ASC,
            orderField = OrderField.DATE,
            showReadItems = false
        )

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        with(query.sql) {
            assertTrue(contains("read = 0"))
            assertTrue(contains("pub_date ASC"))
        }
    }

    @Test
    fun newestSortCaseTest() {
        val queryFilters = QueryFilters(
            accountId = 1,
            orderType = OrderType.DESC,
            orderField = OrderField.ID
        )

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters)
        database.query(query)

        with(query.sql) {
            assertTrue(contains("Item.id DESC"))
        }
    }

    @Test
    fun separateStateTest() {
        val queryFilters = QueryFilters(
            accountId = 1,
            showReadItems = false,
            mainFilter = MainFilter.STARS
        )

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters, true)
        database.query(query)

        with(query.sql) {
            assertFalse(contains("read, starred"))
            assertTrue(contains("ItemState.read = 0 And "))
            assertTrue(contains("ItemState.starred = 1"))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun accountIdExceptionTest() {
        val queryFilters = QueryFilters()
        ItemsQueryBuilder.buildItemsQuery(queryFilters)
    }

    @Test(expected = IllegalArgumentException::class)
    fun filterFeedIdExceptionTest() {
        val queryFilters = QueryFilters(accountId = 1, subFilter = SubFilter.FEED)
        ItemsQueryBuilder.buildItemsQuery(queryFilters)
    }

    @Test
    fun folderFilterExceptionTest() {
        val queryFilters = QueryFilters(accountId = 1, subFilter = SubFilter.FOLDER)
        assertThrows(IllegalArgumentException::class.java) {
            ItemsQueryBuilder.buildItemsQuery(queryFilters)
        }
    }
}