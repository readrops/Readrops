package com.readrops.db.queries

import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.readrops.db.filters.ListSortType
import com.readrops.db.filters.MainFilter
import com.readrops.db.filters.SubFilter

object ItemsQueryBuilder {

    private val COLUMNS = arrayOf(
        "Item.id",
        "Item.remote_id",
        "title",
        "clean_description",
        "image_link",
        "pub_date",
        "link",
        "Feed.name",
        "color",
        "icon_url",
        "read_time",
        "Feed.id as feedId",
        "Feed.account_id",
        "Folder.id as folder_id",
        "Folder.name as folder_name"
    )

    private val SEPARATE_STATE_COLUMNS = arrayOf(
        "case When ItemState.remote_id is NULL Or ItemState.read = 1 Then 1 else 0 End read",
        "case When ItemState.starred = 1 Then 1 else 0 End starred"
    )

    private val OTHER_COLUMNS = arrayOf("read", "starred")

    private val SELECT_ALL_JOIN = """Item INNER JOIN Feed on Item.feed_id = Feed.id
            LEFT JOIN Folder on Feed.folder_id = Folder.id """.trimIndent()

    private const val SEPARATE_STATE_JOIN =
        "LEFT JOIN ItemState On Item.remote_id = ItemState.remote_id"

    private const val ORDER_BY_ASC = "pub_date DESC"

    private const val ORDER_BY_DESC = "pub_date ASC"

    fun buildItemsQuery(queryFilters: QueryFilters, separateState: Boolean): SupportSQLiteQuery =
        buildQuery(queryFilters, separateState)

    fun buildItemsQuery(queryFilters: QueryFilters): SupportSQLiteQuery =
        buildQuery(queryFilters, false)

    private fun buildQuery(queryFilters: QueryFilters, separateState: Boolean): SupportSQLiteQuery =
        with(queryFilters) {
            if (accountId == 0)
                throw IllegalArgumentException("AccountId must be greater than 0")

            if (queryFilters.subFilter == SubFilter.FEED && filterFeedId == 0)
                throw IllegalArgumentException("FeedId must be greater than 0 if current filter is FEED_FILTER")

            val columns = if (separateState)
                COLUMNS.plus(SEPARATE_STATE_COLUMNS)
            else
                COLUMNS.plus(OTHER_COLUMNS)

            val selectAllJoin =
                if (separateState) SELECT_ALL_JOIN + SEPARATE_STATE_JOIN else SELECT_ALL_JOIN

            SupportSQLiteQueryBuilder.builder(selectAllJoin).run {
                columns(columns)
                selection(buildWhereClause(this@with, separateState), null)
                orderBy(if (sortType == ListSortType.NEWEST_TO_OLDEST) ORDER_BY_ASC else ORDER_BY_DESC)

                create()
            }
        }

    private fun buildWhereClause(queryFilters: QueryFilters, separateState: Boolean): String =
        StringBuilder(500).run {
            append("Feed.account_id = ${queryFilters.accountId} ")

            if (!queryFilters.showReadItems) {
                if (separateState)
                    append("And ItemState.read = 0 ")
                else
                    append("And Item.read = 0 ")
            }

            when (queryFilters.mainFilter) {
                MainFilter.STARS -> {
                    if (separateState) {
                        append("And ItemState.starred = 1 ")
                    } else {
                        append("And starred = 1 ")
                    }
                }

                MainFilter.NEW -> append("And DateTime(Round(pub_date / 1000), 'unixepoch') Between DateTime(DateTime(\"now\"), \"-24 hour\") And DateTime(\"now\") ")
                else -> {}
            }

            when (queryFilters.subFilter) {
                SubFilter.FEED -> append("And feed_id = ${queryFilters.filterFeedId} ")
                SubFilter.FOLDER -> append("And folder_id = ${queryFilters.filterFolderId} ")
                else -> {}
            }

            toString()
        }

}

data class QueryFilters(
    val showReadItems: Boolean = true,
    val filterFeedId: Int = 0,
    val filterFolderId: Int = 0,
    val accountId: Int = 0,
    val mainFilter: MainFilter = MainFilter.ALL,
    val subFilter: SubFilter = SubFilter.ALL,
    val sortType: ListSortType = ListSortType.NEWEST_TO_OLDEST,
)