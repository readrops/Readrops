package com.readrops.db.queries

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.readrops.db.filters.FilterType
import org.intellij.lang.annotations.Language

object FoldersAndFeedsQueriesBuilder {

    fun buildFoldersAndFeedsQuery(accountId: Int, filterType: FilterType): SupportSQLiteQuery {
        val filter = when (filterType) {
            FilterType.STARS_FILTER -> "And Item.starred = 1"
            FilterType.NEW -> "And DateTime(Round(Item.pub_date / 1000), 'unixepoch') Between DateTime(DateTime(\"now\"), \"-24 hour\") And DateTime(\"now\") "
            else -> ""
        }

        @Language("SQL")
        val query = SimpleSQLiteQuery("""
            With main As (Select Folder.id As folderId, Folder.name As folderName, Feed.id As feedId, 
            Feed.name As feedName, Feed.icon_url As feedIcon, Feed.url As feedUrl, Feed.siteUrl As feedSiteUrl, 
            Folder.account_id As accountId, Item.read as itemRead
            From Folder Left Join Feed On Folder.id = Feed.folder_id Left Join Item On Item.feed_id = Feed.id 
            Where Feed.folder_id is NULL OR Feed.folder_id is NOT NULL And Feed.account_id = $accountId $filter) 
            Select folderId, folderName, feedId, feedName, feedIcon, feedUrl, feedSiteUrl, accountId,
             (Select count(*) From main Where (itemRead = 0)) as unreadCount
            From main Group by feedId, folderId Order By folderName, feedName
        """.trimIndent())

        return query
    }

    fun buildFeedsWithoutFolderQuery(accountId: Int, filterType: FilterType): SupportSQLiteQuery {
        val filter = when (filterType) {
            FilterType.STARS_FILTER -> "And Item.starred = 1 "
            FilterType.NEW -> "And DateTime(Round(Item.pub_date / 1000), 'unixepoch') Between DateTime(DateTime(\"now\"), \"-24 hour\") And DateTime(\"now\") "
            else -> ""
        }

        @Language("SQL")
        val query = SimpleSQLiteQuery("""
            With main As (Select Feed.id As feedId, Feed.name As feedName, Feed.icon_url As feedIcon, 
            Feed.url As feedUrl, Feed.siteUrl As feedSiteUrl, Feed.account_id As accountId, Item.read As itemRead 
            From Feed Left Join Item On Feed.id = Item.feed_id Where Feed.folder_id is Null And Feed.account_id = $accountId $filter)
            Select feedId, feedName, feedIcon, feedUrl, feedSiteUrl, accountId, 
            (Select count(*) From main Where (itemRead = 0)) as unreadCount From main Group by feedId Order By feedName
        """.trimIndent())

        return query
    }

}