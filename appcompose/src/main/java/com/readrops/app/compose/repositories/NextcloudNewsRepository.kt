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
import org.joda.time.DateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class NextcloudNewsRepository(
    database: Database,
    account: Account,
    private val dataSource: NewNextcloudNewsDataSource
) : BaseRepository(database, account), KoinComponent {

    override suspend fun login(account: Account) {
        get<AuthInterceptor>().apply {
            credentials = Credentials.toCredentials(account)
        }

        val displayName = dataSource.login(get(), account)
        account.displayedName = displayName
    }

    override suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult> = throw NotImplementedError("This method can't be called here")

    override suspend fun synchronize(): SyncResult {
        val itemStateChanges = database.newItemStateChangeDao()
            .selectItemStateChanges(account.id)

        val starredIds = itemStateChanges.filter { it.starChange && it.starred }
            .map { it.remoteId }
        val unstarredIds = itemStateChanges.filter { it.starChange && !it.starred }
            .map { it.remoteId }

        val syncData = NextcloudNewsSyncData(
            lastModified = account.lastModified,
            readIds = itemStateChanges.filter { it.readChange && it.read }
                .map { it.remoteId.toInt() },
            unreadIds = itemStateChanges.filter { it.readChange && !it.read }
                .map { it.remoteId.toInt() },
            starredIds = database.newItemDao().selectStarChanges(starredIds, account.id),
            unstarredIds = database.newItemDao().selectStarChanges(unstarredIds, account.id)
        )

        val syncType = if (account.lastModified != 0L) {
            SyncType.CLASSIC_SYNC
        } else {
            SyncType.INITIAL_SYNC
        }

        val newLastModified = DateTime.now().millis / 1000L

        return dataSource.synchronize(syncType, syncData).apply {
            insertFolders(folders)
            newFeedIds = insertFeeds(feeds)

            insertItems(items, syncType == SyncType.INITIAL_SYNC)

            account.lastModified = newLastModified
            database.newAccountDao().updateLastModified(newLastModified, account.id)

            database.itemStateChangesDao().resetStateChanges(account.id)
        }
    }

    override suspend fun insertNewFeeds(
        newFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult {
        val errors = mutableMapOf<Feed, Exception>()

        for (newFeed in newFeeds) {
            onUpdate(newFeed)

            try {
                val feeds = dataSource.createFeed(newFeed.url!!, null)
                insertFeeds(feeds)
            } catch (e: Exception) {
                errors[newFeed] = e
            }
        }

        return errors
    }

    override suspend fun updateFeed(feed: Feed) {
        val folder =
            if (feed.folderId != null) database.newFolderDao().select(feed.folderId!!) else null

        dataSource.renameFeed(feed.name!!, feed.remoteId!!.toInt())
        dataSource.changeFeedFolder(folder?.remoteId?.toInt(), feed.remoteId!!.toInt())

        super.updateFeed(feed)
    }

    override suspend fun deleteFeed(feed: Feed) {
        dataSource.deleteFeed(feed.remoteId!!.toInt())
        super.deleteFeed(feed)
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