package com.readrops.app.compose.repositories

import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.filters.MainFilter
import com.readrops.db.queries.FoldersAndFeedsQueriesBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFoldersWithFeeds(
    private val database: Database,
) {

    fun get(accountId: Int, mainFilter: MainFilter): Flow<Map<Folder?, List<Feed>>> {
        val foldersAndFeedsQuery =
            FoldersAndFeedsQueriesBuilder.buildFoldersAndFeedsQuery(accountId, mainFilter)
        val feedsWithoutFolderQuery =
            FoldersAndFeedsQueriesBuilder.buildFeedsWithoutFolderQuery(accountId, mainFilter)

        return combine(
            flow = database.newFolderDao()
                .selectFoldersAndFeeds(foldersAndFeedsQuery),
            flow2 = database.newFeedDao()
                .selectFeedsWithoutFolder(feedsWithoutFolderQuery)
        ) { folders, feedsWithoutFolder ->
            val foldersWithFeeds = folders.groupBy(
                keySelector = {
                    Folder(
                        id = it.folderId,
                        name = it.folderName,
                        accountId = it.accountId
                    ) as Folder?
                },
                valueTransform = {
                    Feed(
                        id = it.feedId,
                        name = it.feedName,
                        iconUrl = it.feedIcon,
                        url = it.feedUrl,
                        siteUrl = it.feedSiteUrl,
                        description = it.feedDescription,
                        unreadCount = it.unreadCount
                    )
                }
            ).mapValues { listEntry ->
                if (listEntry.value.any { it.id == 0 }) {
                    listOf()
                } else {
                    listEntry.value
                }
            }

            if (feedsWithoutFolder.isNotEmpty()) {
                foldersWithFeeds + mapOf(
                    Pair(
                        null,
                        feedsWithoutFolder.map { feedWithoutFolder ->
                            Feed(
                                id = feedWithoutFolder.feedId,
                                name = feedWithoutFolder.feedName,
                                iconUrl = feedWithoutFolder.feedIcon,
                                url = feedWithoutFolder.feedUrl,
                                siteUrl = feedWithoutFolder.feedSiteUrl,
                                description = feedWithoutFolder.feedDescription,
                                unreadCount = feedWithoutFolder.unreadCount
                            )
                        })
                )
            } else {
                foldersWithFeeds
            }
        }
    }
}