package com.readrops.db.queries

import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.readrops.db.filters.MainFilter
import com.readrops.db.filters.OrderField
import com.readrops.db.filters.OrderType
import com.readrops.db.filters.QueryFilters
import com.readrops.db.filters.SubFilter

object ItemsQueryBuilder {

    private val COLUMNS = arrayOf(
        "Item.id",
        "Item.remote_id",
        "title",
        "author",
        "clean_description",
        "Item.description",
        "content",
        "image_link",
        "pub_date",
        "link",
        "Feed.name",
        "color",
        "icon_url",
        "read_time",
        "Feed.id as feedId",
        "Feed.account_id",
        "Feed.open_in",
        "Feed.open_in_ask",
        "Folder.id as folder_id",
        "Folder.name as folder_name"
    )

    private val SEPARATE_STATE_COLUMNS = arrayOf(
        "case When ItemState.remote_id is NULL Or ItemState.read = 1 Then 1 else 0 End is_read",
        "case When ItemState.remote_id is NULL Or ItemState.read = 1 Then 1 else 0 End read",
        "case When ItemState.starred = 1 Then 1 else 0 End is_starred",
        "case When ItemState.starred = 1 Then 1 else 0 End starred"
    )

    private val OTHER_COLUMNS = arrayOf("read AS is_read", "read", "starred AS is_starred", "starred")

    private val SELECT_ALL_JOIN = """Item INNER JOIN Feed on Item.feed_id = Feed.id
            LEFT JOIN Folder on Feed.folder_id = Folder.id """.trimIndent()

    private const val SEPARATE_STATE_JOIN =
        "LEFT JOIN ItemState On Item.remote_id = ItemState.remote_id"

    fun buildItemsQuery(queryFilters: QueryFilters, separateState: Boolean): SupportSQLiteQuery =
        buildQuery(queryFilters, separateState)

    fun buildItemsQuery(queryFilters: QueryFilters): SupportSQLiteQuery =
        buildQuery(queryFilters, false)

    private fun buildQuery(queryFilters: QueryFilters, separateState: Boolean): SupportSQLiteQuery =
        with(queryFilters) {
            require(accountId != 0) { "AccountId must be greater than 0" }

            if (subFilter == SubFilter.FEED && feedId == 0) {
                throw IllegalArgumentException("FeedId must be greater than 0 if subFilter is FEED")
            } else if (subFilter == SubFilter.FOLDER && folderId == 0) {
                throw IllegalArgumentException("FolderId must be greater than 0 if subFilter is FOLDER")
            }

            val columns = if (separateState) {
                COLUMNS.plus(SEPARATE_STATE_COLUMNS)
            } else {
                COLUMNS.plus(OTHER_COLUMNS)
            }

            val selectAllJoin = if (separateState) {
                SELECT_ALL_JOIN + SEPARATE_STATE_JOIN
            } else {
                SELECT_ALL_JOIN
            }

            SupportSQLiteQueryBuilder.builder(selectAllJoin).run {
                columns(columns)
                selection(buildWhereClause(this@with, separateState), null)
                orderBy(buildOrderByClause(orderField, orderType))

                create()
            }
        }

    private fun buildWhereClause(queryFilters: QueryFilters, separateState: Boolean): String =
        buildString {
            append("Feed.account_id = ${queryFilters.accountId} ")

            if (!queryFilters.showReadItems) {
                if (separateState) {
                    append("And ItemState.read = 0 ")
                } else {
                    append("And Item.read = 0 ")
                }
            }

            when (queryFilters.mainFilter) {
                MainFilter.STARS -> {
                    if (separateState) {
                        append("And ItemState.starred = 1 ")
                    } else {
                        append("And starred = 1 ")
                    }
                }

                MainFilter.NEW -> append("""And DateTime(Round(pub_date / 1000), 'unixepoch') 
                    Between DateTime(DateTime("now"), "-24 hour") And DateTime("now")""".trimMargin())
                else -> {}
            }

            when (queryFilters.subFilter) {
                SubFilter.FEED -> append("And feed_id = ${queryFilters.feedId} ")
                SubFilter.FOLDER -> append("And folder_id = ${queryFilters.folderId} ")
                else -> {}
            }

            toString()
        }

    private fun buildOrderByClause(orderField: OrderField, orderType: OrderType): String {
        return buildString {
            when (orderField) {
                OrderField.ID -> append("Item.id ")
                else -> append("pub_date ")
            }

            when (orderType) {
                OrderType.DESC -> append("DESC")
                else -> append("ASC")
            }
        }
    }
}

