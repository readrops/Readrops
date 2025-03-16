package com.readrops.db.queries

import androidx.room.util.newStringBuilder
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder

object FoldersAndFeedsQueryBuilder {

    private val COLUMNS = arrayOf(
        "Feed.id As feedId",
        "Feed.name As feedName",
        "Feed.icon_url As feedIcon",
        "Feed.color As feedColor",
        "Feed.url As feedUrl",
        "Feed.image_url as feedImage",
        "Feed.siteUrl As feedSiteUrl",
        "Feed.description as feedDescription",
        "Feed.notification_enabled as feedNotificationsEnabled",
        "Feed.open_in as feedOpenIn",
        "Feed.remote_id as feedRemoteId",
        "Folder.id As folderId",
        "Folder.name As folderName",
        "Feed.account_id as accountId",
        "Folder.remoteId as folderRemoteId"
    )

    private val FEED_JOIN = """(Select * From Feed Where account_id = :accountId) Feed 
        Left Join Folder On Folder.id = Feed.folder_id""".trimMargin()

    private val SEPARATE_STATE = " Left Join ItemState On ItemState.remote_id = Item.remote_id"

    private const val FOLDER_JOIN = "Folder Left Join Feed On Folder.id = Feed.folder_id "

    private const val ITEM_JOIN = " Inner Join Item On Item.feed_id = Feed.id "

    private const val FEED_SELECTION = "Feed.folder_id is NULL OR Feed.folder_id is NOT NULL "

    private const val FOLDER_SELECTION = "Feed.id is NULL And Folder.account_id = :accountId"

    fun build(accountId: Int, hideReadFeeds: Boolean, useSeparateState: Boolean): SupportSQLiteQuery {
        val result = SimpleSQLiteQuery(
            """
            ${buildFeedQuery(accountId, hideReadFeeds, useSeparateState).sql}
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
        return result
    }

    private fun buildFeedQuery(accountId: Int, hideReadFeeds: Boolean, useSeparateState: Boolean): SupportSQLiteQuery {
        val tables = buildString {
            append(FEED_JOIN)
            if (hideReadFeeds) {
                append(ITEM_JOIN)
                if(useSeparateState) append(SEPARATE_STATE)
            }
        }
        val selection = buildString {
            append(FEED_SELECTION)
            if (hideReadFeeds) {
                if(useSeparateState) append("AND ItemState.read = 0")
                else append("And Item.read = 0")
            }
        }

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