package com.readrops.app.compose.repositories

import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFoldersWithFeeds(
    private val database: Database,
) {

    fun get(accountId: Int): Flow<Map<Folder?, List<Feed>>> {
        return combine(
            flow = database.newFolderDao()
                .selectFoldersAndFeeds(accountId),
            flow2 = database.newFeedDao()
                .selectFeedsWithoutFolder(accountId)
        ) { folders, feedsWithoutFolder ->
            val foldersWithFeeds = folders.groupBy(
                keySelector = { Folder(id = it.folderId, name = it.folderName) },
                valueTransform = {
                    Feed(
                        id = it.feedId,
                        name = it.feedName,
                        iconUrl = it.feedIcon,
                        siteUrl = it.feedUrl,
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

            foldersWithFeeds + mapOf(
                Pair(
                    null,
                    feedsWithoutFolder.map { it.feed.apply { unreadCount = it.unreadCount } })
            )
        }
    }
}