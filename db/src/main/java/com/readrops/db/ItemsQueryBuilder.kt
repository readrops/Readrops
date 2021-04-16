package com.readrops.db

import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.readrops.db.filters.FilterType
import com.readrops.db.filters.ListSortType

object ItemsQueryBuilder {

    private val COLUMNS = arrayOf("title", "clean_description", "image_link", "pub_date",
            "read_it_later", "Feed.name", "text_color", "background_color", "icon_url", "read_time",
            "Feed.id as feedId", "Feed.account_id", "Folder.id as folder_id", "Folder.name as folder_name",
            "case When UnreadItemsIds.remote_id is NULL Then 1 else 0 End read")

    private val ITEM_COLUMNS = arrayOf(".id", ".remoteId")

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

        val tableName = tableName(starQuery)
        val selectAllJoin = buildSelectAllJoin(tableName)

        SupportSQLiteQueryBuilder.builder(selectAllJoin).run {
            columns(COLUMNS.plus(buildItemColumns(tableName)))
            selection(buildWhereClause(this@with), null)
            orderBy(if (sortType == ListSortType.NEWEST_TO_OLDEST) buildOrderByAsc(tableName) else ORDER_BY_DESC)

            create()
        }
    }

    private fun buildWhereClause(queryFilters: QueryFilters): String = StringBuilder(500).run {
        append("Feed.account_id = ${queryFilters.accountId} And " +
                "UnreadItemsIds.account_id = ${queryFilters.accountId} Or UnreadItemsIds.account_id is NULL And ")

        if (!queryFilters.showReadItems) append("read = 0 And ")

        when (queryFilters.filterType) {
            FilterType.FEED_FILTER -> append("feed_id = ${queryFilters.filterFeedId} And read_it_later = 0")
            FilterType.READ_IT_LATER_FILTER -> append("read_it_later = 1")
            FilterType.STARS_FILTER -> append("starred = 1 And read_it_later = 0")
            else -> append("read_it_later = 0")
        }

        toString()
    }

    private fun tableName(starQuery: Boolean): String = if (starQuery) "StarredItem" else "Item"

    private fun buildItemColumns(tableName: String): Array<String> {
        val columns = arrayListOf<String>()

        for (column in ITEM_COLUMNS) {
            columns += tableName + column
        }

        return columns.toTypedArray()
    }

    private fun buildOrderByAsc(tableName: String): String = tableName + ORDER_BY_ASC

    private fun buildSelectAllJoin(tableName: String): String = """
        $tableName INNER JOIN Feed on $tableName.feed_id = Feed.id
            LEFT JOIN Folder on Feed.folder_id = Folder.id LEFT JOIN UnreadItemsIds On
            $tableName.remoteId = UnreadItemsIds.remote_id
    """.trimIndent()

}

class QueryFilters(
        var showReadItems: Boolean = true,
        var filterFeedId: Int = 0,
        var accountId: Int = 0,
        var filterType: FilterType = FilterType.NO_FILTER,
        var sortType: ListSortType = ListSortType.NEWEST_TO_OLDEST,
)