package com.readrops.db.queries

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder

object FoldersAndFeedsQueryBuilder {

    private val COLUMNS = arrayOf(
        "Feed.id As feedId",
        "Feed.name As feedName",
        "Feed.icon_url As feedIcon",
        "Feed.url As feedUrl",
        "Feed.siteUrl As feedSiteUrl",
        "Feed.description as feedDescription",
        "Feed.remoteId as feedRemoteId",
        "Folder.id As folderId",
        "Folder.name As folderName",
        "Feed.account_id as accountId",
        "Folder.remoteId as folderRemoteId"
    )

    private val FEED_JOIN = """(Select * From Feed Where account_id = :accountId) Feed 
        Left Join Folder On Folder.id = Feed.folder_id """.trimMargin()

    private const val FOLDER_JOIN = "Folder Left Join Feed On Folder.id = Feed.folder_id "

    private const val ITEM_JOIN = " Inner Join Item On Item.feed_id = Feed.id "

    private const val FEED_SELECTION = "Feed.folder_id is NULL OR Feed.folder_id is NOT NULL "

    private const val FOLDER_SELECTION = "Feed.id is NULL And Folder.account_id = :accountId"

    private const val ITEM_SELECTION = "And Item.read = 0"

    fun build(accountId: Int, hideReadFeeds: Boolean = false): SupportSQLiteQuery {
        return SimpleSQLiteQuery(
            """
            ${buildFeedQuery(accountId, hideReadFeeds).sql}
            ${
                if (!hideReadFeeds) {
                    """UNION ALL
                        ${buildFolderQuery(accountId).sql}
                    """.trimIndent()
                } else {
                    ""
                }
            }""".trimIndent()
        )
    }

    private fun buildFeedQuery(accountId: Int, hideReadFeeds: Boolean): SupportSQLiteQuery {
        val tables = if (hideReadFeeds) FEED_JOIN + ITEM_JOIN else FEED_JOIN
        val selection = if (hideReadFeeds) FEED_SELECTION + ITEM_SELECTION else FEED_SELECTION

        return SupportSQLiteQueryBuilder.builder(tables.replace(":accountId", "$accountId")).run {
            columns(COLUMNS)
            selection(selection, null)
            groupBy("Feed.id")

            create()
        }
    }

    private fun buildFolderQuery(accountId: Int): SupportSQLiteQuery {
        return SupportSQLiteQueryBuilder.builder(FOLDER_JOIN).run {
            columns(COLUMNS)
            selection(FOLDER_SELECTION.replace(":accountId", "$accountId"), null)

            create()
        }
    }
}