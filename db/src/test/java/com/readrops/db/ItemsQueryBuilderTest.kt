package com.readrops.db

import com.readrops.db.filters.FilterType
import com.readrops.db.filters.ListSortType
import com.readrops.db.queries.ItemsQueryBuilder
import com.readrops.db.queries.QueryFilters
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ItemsQueryBuilderTest {

    @Test
    fun noFilterDefaultSortCaseTest() {
        val queryFilters = QueryFilters(accountId = 1)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters).sql

        assertTrue(query.contains("Feed.account_id = 1"))
        assertTrue(query.contains("read_it_later = 0"))
        assertTrue(query.contains("pub_date DESC"))

        assertFalse(query.contains("read = 0 And"))
    }

    @Test
    fun feedFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, filterType = FilterType.FEED_FILTER,
                filterFeedId = 15)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters).sql

        assertTrue(query.contains("feed_id = 15 And read_it_later = 0"))
    }

    @Test
    fun readLaterFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, filterType = FilterType.READ_IT_LATER_FILTER)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters).sql
        assertTrue(query.contains("read_it_later = 1"))
    }

    @Test
    fun starsFilterCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, filterType = FilterType.STARS_FILTER)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters).sql
        assertTrue(query.contains("starred = 1 And read_it_later = 0"))
    }

    @Test
    fun oldestSortCaseTest() {
        val queryFilters = QueryFilters(accountId = 1, sortType = ListSortType.OLDEST_TO_NEWEST,
                showReadItems = false)

        val query = ItemsQueryBuilder.buildItemsQuery(queryFilters).sql

        assertTrue(query.contains("read = 0 And "))
        assertTrue(query.contains("pub_date ASC"))
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