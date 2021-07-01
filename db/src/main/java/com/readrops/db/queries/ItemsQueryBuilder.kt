package com.readrops.db.queries

import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.readrops.db.filters.FilterType
import com.readrops.db.filters.ListSortType

object ItemsQueryBuilder {

    private val COLUMNS = arrayOf("Item.id", "Item.remoteId", "title", "clean_description", "image_link", "pub_date",
            "read_it_later", "Feed.name", "text_color", "background_color", "icon_url", "read_time",
            "Feed.id as feedId", "Feed.account_id", "Folder.id as folder_id", "Folder.name as folder_name")

    private val SEPARATE_STATE_COLUMNS = arrayOf("case When ItemState.remote_id is NULL Or ItemState.read = 1 Then 1 else 0 End read",
            "case When ItemState.remote_id is NULL or ItemState.starred = 1 Then 1 else 0 End starred")

    private val OTHER_COLUMNS = arrayOf("read", "starred")

    private val SELECT_ALL_JOIN = """Item INNER JOIN Feed on Item.feed_id = Feed.id
            LEFT JOIN Folder on Feed.folder_id = Folder.id """.trimIndent()

    private const val SEPARATE_STATE_JOIN = "LEFT JOIN ItemState On Item.remoteId = ItemState.remote_id"

    private const val ORDER_BY_ASC = "Item.id DESC"

    private const val ORDER_BY_DESC = "pub_date ASC"

    @JvmStatic
    fun buildItemsQuery(queryFilters: QueryFilters, separateState: Boolean): SupportSQLiteQuery =
            buildQuery(queryFilters, separateState)

    @JvmStatic
    fun buildItemsQuery(queryFilters: QueryFilters): SupportSQLiteQuery =
            buildQuery(queryFilters, false)

    private fun buildQuery(queryFilters: QueryFilters, separateState: Boolean): SupportSQLiteQuery = with(queryFilters) {
        if (accountId == 0)
            throw IllegalArgumentException("AccountId must be greater than 0")

        if (filterType == FilterType.FEED_FILTER && filterFeedId == 0)
            throw IllegalArgumentException("FeedId must be greater than 0 if current filter is FEED_FILTER")

        val columns = if (separateState) COLUMNS.plus(SEPARATE_STATE_COLUMNS) else COLUMNS.plus(OTHER_COLUMNS)
        val selectAllJoin = if (separateState) SELECT_ALL_JOIN + SEPARATE_STATE_JOIN else SELECT_ALL_JOIN

        SupportSQLiteQueryBuilder.builder(selectAllJoin).run {
            columns(columns)
            selection(buildWhereClause(this@with, separateState), null)
            orderBy(if (sortType == ListSortType.NEWEST_TO_OLDEST) ORDER_BY_ASC else ORDER_BY_DESC)

            create()
        }
    }

    private fun buildWhereClause(queryFilters: QueryFilters, separateState: Boolean): String = StringBuilder(500).run {
        append("Feed.account_id = ${queryFilters.accountId} And ")

        if (!queryFilters.showReadItems) {
            if (separateState)
                append("ItemState.read = 0 And ")
            else
                append("Item.read = 0 And ")
        }

        when (queryFilters.filterType) {
            FilterType.FEED_FILTER -> append("feed_id = ${queryFilters.filterFeedId} And read_it_later = 0")
            FilterType.READ_IT_LATER_FILTER -> append("read_it_later = 1")
            FilterType.STARS_FILTER -> {
                if (separateState) {
                    append("ItemState.remote_id is NULL or ItemState.starred = 1 And read_it_later = 0")
                } else {
                    append("starred = 1 And read_it_later = 0")
                }
            }
            else -> append("read_it_later = 0")
        }

        toString()
    }

}

class QueryFilters(
        var showReadItems: Boolean = true,
        var filterFeedId: Int = 0,
        var accountId: Int = 0,
        var filterType: FilterType = FilterType.NO_FILTER,
        var sortType: ListSortType = ListSortType.NEWEST_TO_OLDEST,
)