package com.readrops.app.repositories

import com.readrops.api.services.Credentials
import com.readrops.api.services.SyncType
import com.readrops.api.services.nextcloudnews.NextcloudNewsDataSource
import com.readrops.api.services.nextcloudnews.NextcloudNewsSyncData
import com.readrops.api.utils.AuthInterceptor
import com.readrops.app.util.Utils
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class NextcloudNewsRepository(
    database: Database,
    account: Account,
    private val dataSource: NextcloudNewsDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
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
        onUpdate: suspend (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult> = throw NotImplementedError("This method can't be called here")

    override suspend fun synchronize(): SyncResult {
        val itemStateChanges = database.itemStateChangeDao()
            .selectItemStateChanges(account.id)

        val syncData = NextcloudNewsSyncData(
            lastModified = account.lastModified,
            readIds = itemStateChanges.filter { it.readChange && it.read }
                .map { it.remoteId.toInt() },
            unreadIds = itemStateChanges.filter { it.readChange && !it.read }
                .map { it.remoteId.toInt() },
            starredIds = itemStateChanges.filter { it.starChange && it.starred }
                .map { it.remoteId.toInt() },
            unstarredIds = itemStateChanges.filter { it.starChange && !it.starred }
                .map { it.remoteId.toInt() }
        )

        val syncType = if (account.lastModified != 0L) {
            SyncType.CLASSIC_SYNC
        } else {
            SyncType.INITIAL_SYNC
        }

        val newLastModified = System.currentTimeMillis() / 1000L

        return dataSource.synchronize(syncType, syncData).run {
            insertFolders(folders)
            val newFeeds = insertFeeds(feeds)

            val initialSync = syncType == SyncType.INITIAL_SYNC
            val newItems = insertItems(items, initialSync)
            insertItems(starredItems, initialSync)

            account.lastModified = newLastModified
            database.accountDao().updateLastModified(newLastModified, account.id)

            database.itemStateChangeDao().resetStateChanges(account.id)

            SyncResult(
                items = newItems,
                feeds = newFeeds
            )
        }
    }

    override suspend fun insertNewFeeds(
        newFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult {
        val errors = hashMapOf<Feed, Exception>()

        for (newFeed in newFeeds) {
            onUpdate(newFeed)

            try {
                dataSource.createFeed(newFeed.url!!, newFeed.remoteFolderId?.toInt())
                    .onEach {
                        it.accountId = account.id
                        it.folderId = newFeed.folderId
                    }
                    .run { database.feedDao().insert(this) }
            } catch (e: Exception) {
                errors[newFeed] = e
            }
        }

        return errors
    }

    override suspend fun updateFeed(feed: Feed) = withContext(dispatcher) {
        val folder =
            if (feed.folderId != null) database.folderDao().select(feed.folderId!!) else null

        listOf(
            async { dataSource.renameFeed(feed.name!!, feed.remoteId!!.toInt()) },
            async { dataSource.changeFeedFolder(folder?.remoteId?.toInt(), feed.remoteId!!.toInt()) }
        ).awaitAll()

        super.updateFeed(feed)
    }

    override suspend fun deleteFeed(feed: Feed) {
        dataSource.deleteFeed(feed.remoteId!!.toInt())
        super.deleteFeed(feed)
    }

    override suspend fun addFolder(folder: Folder) {
        val folders = dataSource.createFolder(folder.name!!)
            .onEach { it.accountId = account.id }

        database.folderDao().insert(folders)
    }

    override suspend fun updateFolder(folder: Folder) {
        dataSource.renameFolder(folder.name!!, folder.remoteId!!.toInt())
        super.updateFolder(folder)
    }

    override suspend fun deleteFolder(folder: Folder) {
        dataSource.deleteFolder(folder.remoteId!!.toInt())
        super.deleteFolder(folder)
    }

    private suspend fun insertFolders(folders: List<Folder>) {
        folders.forEach { it.accountId = account.id }
        database.folderDao().upsertFolders(folders, account)
    }

    private suspend fun insertFeeds(feeds: List<Feed>): List<Feed> {
        feeds.forEach { it.accountId = account.id }
        return database.feedDao().upsertFeeds(feeds, account)
    }

    private suspend fun insertItems(items: List<Item>, initialSync: Boolean): List<Item> {
        val newItems = arrayListOf<Item>()
        val itemsFeedsIds = mutableMapOf<String?, Int>()

        for (item in items) {
            val feedId: Int
            if (itemsFeedsIds.containsKey(item.feedRemoteId)) {
                feedId = itemsFeedsIds.getValue(item.feedRemoteId)
            } else {
                feedId =
                    database.feedDao().selectRemoteFeedLocalId(item.feedRemoteId!!, account.id)
                itemsFeedsIds[item.feedRemoteId] = feedId
            }

            if (!initialSync && feedId > 0 && database.itemDao().itemExists(item.remoteId!!, account.id)) {
                database.itemDao()
                    .updateReadAndStarState(item.remoteId!!, item.isRead, item.isStarred)
                continue
            }

            item.feedId = feedId
            item.readTime = Utils.readTimeFromString(item.content.orEmpty())
            newItems += item
        }

        if (newItems.isNotEmpty()) {
            database.itemDao().insert(newItems)
                .zip(newItems)
                .forEach { (id, item) -> item.id = id.toInt() }
        }

        return newItems
    }
}