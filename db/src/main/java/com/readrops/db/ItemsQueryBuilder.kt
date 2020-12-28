package com.readrops.db

import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.readrops.db.filters.FilterType
import com.readrops.db.filters.ListSortType

object ItemsQueryBuilder {

    private val COLUMNS = arrayOf("title", "clean_description", "image_link", "pub_date", "read",
            "read_changed", "read_it_later", "Feed.name", "text_color", "background_color", "icon_url", "read_time",
            "Feed.id as feedId", "Feed.account_id", "Folder.id as folder_id", "Folder.name as folder_name")

    private val ITEM_COLUMNS = arrayOf(".id", ".remoteId")

    private const val SELECT_ALL_JOIN = "Item INNER JOIN Feed on Item.feed_id = Feed.id " +
            "LEFT JOIN Folder on Feed.folder_id = Folder.id"

    private const val ORDER_BY_ASC = ".id DESC"

    private const val ORDER_BY_DESC = "pub_date ASC"

    @JvmStatic
    fun buildItemsQuery(queryFilters: QueryFilters): SupportSQLiteQuery =
            buildQuery(queryFilters, false)

    @JvmStatic
    fun buildStarredItemsQuery(queryFilters: QueryFilters): SupportSQLiteQuery =
            buildQuery(queryFilters, true)

    private fun buildQuery(queryFilters: QueryFilters, starQuery: Boolean): SupportSQLiteQuery = with(queryFilters) {
        if (accountId == 0)
            throw IllegalArgumentException("AccountId must be greater than 0")

        if (filterType == FilterType.FEED_FILTER && filterFeedId == 0)
            throw IllegalArgumentException("FeedId must be greater than 0 if current filter is FEED_FILTER")

        SupportSQLiteQueryBuilder.builder(if (starQuery) SELECT_ALL_JOIN.replace("Item", "StarredItem") else SELECT_ALL_JOIN).run {
            columns(COLUMNS.plus(buildItemColumns(starQuery)))
            selection(buildWhereClause(this@with), null)
            orderBy(if (sortType == ListSortType.NEWEST_TO_OLDEST) buildOrderByAsc(starQuery) else ORDER_BY_DESC)

            create()
        }
    }

    private fun buildWhereClause(queryFilters: QueryFilters): String {
        return StringBuilder(500).run {
            append("Feed.account_id = ${queryFilters.accountId} And ")

            if (!queryFilters.showReadItems) append("read = 0 And ")

            when (queryFilters.filterType) {
                FilterType.FEED_FILTER -> append("feed_id = ${queryFilters.filterFeedId} And read_it_later = 0")
                FilterType.READ_IT_LATER_FILTER -> append("read_it_later = 1")
                FilterType.STARS_FILTER -> append("starred = 1 And read_it_later = 0")
                else -> append("read_it_later = 0")
            }

            toString()
        }
    }

    private fun buildItemColumns(starQuery: Boolean): Array<String> {
        val columns = arrayListOf<String>()

        for (column in ITEM_COLUMNS) {
            columns += if (starQuery) "StarredItem$column" else "Item$column"
        }

        return columns.toTypedArray()
    }

    private fun buildOrderByAsc(starQuery: Boolean): String =
            if (starQuery) "StarredItem$ORDER_BY_ASC" else "Item$ORDER_BY_ASC"

}

class QueryFilters(var showReadItems: Boolean = true,
                   var filterFeedId: Int = 0,
                   var accountId: Int = 0,
                   var filterType: FilterType = FilterType.NO_FILTER,
                   var sortType: ListSortType = ListSortType.NEWEST_TO_OLDEST)