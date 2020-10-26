package com.readrops.db

import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.readrops.db.filters.FilterType
import com.readrops.db.filters.ListSortType

object ItemsQueryBuilder {

    private val COLUMNS = arrayOf("Item.id", "title", "clean_description", "image_link", "pub_date", "read",
            "read_changed", "read_it_later", "Feed.name", "text_color", "background_color", "icon_url", "read_time", "Item.remoteId",
            "Feed.id as feedId", "Feed.account_id", "Folder.id as folder_id", "Folder.name as folder_name")

    private const val SELECT_ALL_JOIN = "Item INNER JOIN Feed on Item.feed_id = Feed.id " +
            "LEFT JOIN Folder on Feed.folder_id = Folder.id"

    private const val ORDER_BY_ASC = "Item.id DESC"

    private const val ORDER_BY_DESC = "pub_date ASC"


    @JvmStatic
    fun buildQuery(queryFilters: QueryFilters): SupportSQLiteQuery {
        if (queryFilters.accountId == 0)
            throw IllegalArgumentException("AccountId must be greater than 0")

        if (queryFilters.filterType == FilterType.FEED_FILTER && queryFilters.filterFeedId == 0)
            throw IllegalArgumentException("FeedId must be greater than 0 if current filter is FEED_FILTER")

        return SupportSQLiteQueryBuilder.builder(SELECT_ALL_JOIN).run {
            columns(COLUMNS)
            selection(buildWhereClause(queryFilters), null)
            orderBy(if (queryFilters.sortType == ListSortType.NEWEST_TO_OLDEST) ORDER_BY_ASC else ORDER_BY_DESC)

            create()
        }
    }

    private fun buildWhereClause(queryFilters: QueryFilters): String? {
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
}

class QueryFilters(var showReadItems: Boolean = true,
                   var filterFeedId: Int = 0,
                   var accountId: Int = 0,
                   var filterType: FilterType = FilterType.NO_FILTER,
                   var sortType: ListSortType = ListSortType.NEWEST_TO_OLDEST)