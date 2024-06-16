package com.readrops.app.compose.repositories

import com.readrops.api.services.Credentials
import com.readrops.api.services.SyncResult
import com.readrops.api.services.SyncType
import com.readrops.api.services.nextcloudnews.NewNextcloudNewsDataSource
import com.readrops.api.services.nextcloudnews.NextcloudNewsSyncData
import com.readrops.api.utils.AuthInterceptor
import com.readrops.app.compose.util.Utils
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class NextcloudNewsRepository(
    database: Database,
    account: Account,
    private val dataSource: NewNextcloudNewsDataSource
) : BaseRepository(database, account), KoinComponent {

    override suspend fun login(account: Account) {
        val authInterceptor = get<AuthInterceptor>()
        authInterceptor.credentials = Credentials.toCredentials(account)

        val displayName = dataSource.login(get(), account)
        account.displayedName = displayName
    }

    override suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult> = throw NotImplementedError("This method can't be called here")

    override suspend fun synchronize(): SyncResult {
        return dataSource.synchronize(SyncType.INITIAL_SYNC, NextcloudNewsSyncData()).apply {
            insertFolders(folders)
            insertFeeds(feeds)

            insertItems(items, true)
        }
    }

    override suspend fun insertNewFeeds(
        newFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult {
        TODO("Not yet implemented")
    }

    private suspend fun insertFolders(folders: List<Folder>) {
        folders.forEach { it.accountId = account.id }
        database.newFolderDao().upsertFolders(folders, account)
    }

    private suspend fun insertFeeds(feeds: List<Feed>): List<Long> {
        feeds.forEach { it.accountId = account.id }
        return database.newFeedDao().upsertFeeds(feeds, account)
    }

    private suspend fun insertItems(items: List<Item>, initialSync: Boolean) {
        val itemsToInsert = arrayListOf<Item>()
        val itemsFeedsIds = mutableMapOf<String?, Int>()

        for (item in items) {
            val feedId: Int
            if (itemsFeedsIds.containsKey(item.feedRemoteId)) {
                feedId = itemsFeedsIds.getValue(item.feedRemoteId)
            } else {
                feedId =
                    database.newFeedDao().selectRemoteFeedLocalId(item.feedRemoteId!!, account.id)
                itemsFeedsIds[item.feedRemoteId] = feedId
            }

            if (!initialSync && feedId > 0 && database.newItemDao()
                    .itemExists(item.remoteId!!, feedId)
            ) {
                database.newItemDao()
                    .updateReadAndStarState(item.remoteId!!, item.isRead, item.isStarred)
            }

            item.feedId = feedId
            item.readTime = Utils.readTimeFromString(item.content.orEmpty())
            itemsToInsert += item
        }

        if (itemsToInsert.isNotEmpty()) {
            database.newItemDao().insert(itemsToInsert)
        }
    }
}