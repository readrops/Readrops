package com.readrops.db.queries

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.filters.MainFilter
import org.intellij.lang.annotations.Language

object FeedUnreadItemsCountQueryBuilder {

    fun build(accountId: Int, mainFilter: MainFilter): SupportSQLiteQuery {
        val filter = when (mainFilter) {
            MainFilter.STARS -> "And Item.starred = 1"
            MainFilter.NEW -> "And DateTime(Round(Item.pub_date / 1000), 'unixepoch') Between DateTime(DateTime(\"now\"), \"-24 hour\") And DateTime(\"now\") "
            else -> ""
        }

        @Language("SQL")
        val query = SimpleSQLiteQuery("""
            Select feed_id, count(*) AS item_count From Item Inner Join Feed On Feed.id = Item.feed_id 
            Where read = 0 And account_id = $accountId $filter Group By feed_id 
        """.trimIndent())

        return query
    }
}