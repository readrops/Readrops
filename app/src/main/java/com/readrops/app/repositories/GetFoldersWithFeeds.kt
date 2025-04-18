package com.readrops.app.repositories

import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.OpenIn
import com.readrops.db.filters.MainFilter
import com.readrops.db.queries.FeedUnreadCountQueryBuilder
import com.readrops.db.queries.FoldersAndFeedsQueryBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFoldersWithFeeds(
    private val database: Database,
) {

    fun get(
        accountId: Int,
        mainFilter: MainFilter,
        useSeparateState: Boolean,
        hideReadFeeds: Boolean = false
    ): Flow<Map<Folder?, List<Feed>>> {
        val foldersAndFeedsQuery = FoldersAndFeedsQueryBuilder.build(accountId, mainFilter, hideReadFeeds, useSeparateState)
        val unreadItemsCountQuery = FeedUnreadCountQueryBuilder.build(accountId, mainFilter, useSeparateState)

        return combine(
            flow = database.folderDao().selectFoldersAndFeeds(foldersAndFeedsQuery),
            flow2 = database.itemDao().selectFeedUnreadItemsCount(unreadItemsCountQuery)
        ) { folders, itemCounts ->
            val foldersWithFeeds = folders.groupBy(
                keySelector = {
                    if (it.folderId != null) {
                        Folder(
                            id = it.folderId!!,
                            name = it.folderName,
                            remoteId = it.folderRemoteId,
                            accountId = it.accountId
                        )
                    } else {
                        null
                    }
                },
                valueTransform = {
                    Feed(
                        id = it.feedId,
                        name = it.feedName,
                        iconUrl = it.feedIcon,
                        color = it.feedColor,
                        imageUrl = it.feedImage,
                        url = it.feedUrl,
                        siteUrl = it.feedSiteUrl,
                        description = it.feedDescription,
                        isNotificationEnabled = it.feedNotificationsEnabled,
                        openIn = if (it.feedOpenIn != null) {
                            it.feedOpenIn!!
                        } else {
                            OpenIn.LOCAL_VIEW
                        },
                        remoteId = it.feedRemoteId,
                        unreadCount = itemCounts[it.feedId] ?: 0
                    )
                }
            ).mapValues { listEntry ->
                // Empty folder case
                if (listEntry.value.any { it.id == 0 }) {
                    listOf()
                } else {
                    listEntry.value
                }
            }

            // Nextcloud News case, no need to add a config parameter
            val comparator = compareByDescending<Folder?> {
                it?.name?.startsWith("_")
            }
                .then(nullsLast(Folder::compareTo))

            foldersWithFeeds.toSortedMap(comparator)
        }
    }

    fun getNewItemsUnreadCount(accountId: Int, useSeparateState: Boolean): Flow<Int> =
        if (useSeparateState) {
            database.itemDao().selectUnreadNewItemsCountByItemState(accountId)
        } else {
            database.itemDao().selectUnreadNewItemsCount(accountId)
        }
}