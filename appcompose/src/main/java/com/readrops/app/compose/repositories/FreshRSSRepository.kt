package com.readrops.app.compose.repositories

import com.readrops.api.services.Credentials
import com.readrops.api.services.SyncResult
import com.readrops.api.services.SyncType
import com.readrops.api.services.freshrss.NewFreshRSSDataSource
import com.readrops.api.utils.AuthInterceptor
import com.readrops.app.compose.util.Utils
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import org.koin.core.component.KoinComponent

class FreshRSSRepository(
    database: Database,
    account: Account,
    private val dataSource: NewFreshRSSDataSource,
) : BaseRepository(database, account), KoinComponent {

    override suspend fun login(account: Account) {
        val authInterceptor = getKoin().get<AuthInterceptor>()
        authInterceptor.credentials = Credentials.toCredentials(account)

        val authToken = dataSource.login(account.login!!, account.password!!)
        account.token = authToken
        // we got the authToken, time to provide it to make real calls
        authInterceptor.credentials = Credentials.toCredentials(account)

        val userInfo = dataSource.getUserInfo()
        account.displayedName = userInfo.userName
    }

    override suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult> {
        TODO("Not yet implemented")
    }

    override suspend fun synchronize(): SyncResult {
        val syncResult = dataSource.synchronise(SyncType.INITIAL_SYNC).apply {
            insertFolders(folders)
            insertFeeds(feeds)

            insertItems(items, false)
            insertItems(starredItems, true)
        }

        return syncResult
    }

    override suspend fun insertNewFeeds(
        newFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult {
        TODO("Not yet implemented")
    }

    private suspend fun insertFeeds(feeds: List<Feed>) {
        feeds.forEach { it.accountId = account.id }
        database.newFeedDao().upsertFeeds(feeds, account)
    }

    private suspend fun insertFolders(folders: List<Folder>) {
        folders.forEach { it.accountId = account.id }
        database.newFolderDao().upsertFolders(folders, account)
    }

    private suspend fun insertItems(items: List<Item>, starredItems: Boolean) {
        val itemsToInsert = arrayListOf<Item>()
        val itemsFeedsIds = mutableMapOf<String?, Int>()

        for (item in items) {
            val feedId: Int
            if (itemsFeedsIds.containsKey(item.feedRemoteId)) {
                feedId = itemsFeedsIds.getValue(item.feedRemoteId)
            } else {
                feedId = database.newFeedDao().selectRemoteFeedLocalId(item.feedRemoteId!!, account.id)
                itemsFeedsIds[item.feedRemoteId] = feedId
            }

            item.feedId = feedId

            if (item.text != null) {
                item.readTime = Utils.readTimeFromString(item.text!!)
            }

            // workaround to avoid inserting starred items coming from the main item call
            // as the API exclusion filter doesn't seem to work
            if (!starredItems) {
                if (!item.isStarred) {
                    itemsToInsert.add(item)
                }
            } else {
                itemsToInsert.add(item)
            }
        }

        if (itemsToInsert.isNotEmpty()) {
            itemsToInsert.sortWith(Item::compareTo)
            database.itemDao().insert(itemsToInsert)
        }
    }
}