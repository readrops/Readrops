package com.readrops.api.services.nextcloudnews

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.services.SyncResult
import com.readrops.api.services.SyncType
import com.readrops.api.services.nextcloudnews.adapters.NextNewsUserAdapter
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import okhttp3.OkHttpClient
import okhttp3.Request

class NewNextcloudNewsDataSource(private val service: NewNextcloudNewsService) {

    suspend fun login(client: OkHttpClient, account: Account): String {
        val request = Request.Builder()
            .url(account.url + "/ocs/v1.php/cloud/users/" + account.login)
            .addHeader("OCS-APIRequest", "true")
            .build()

        val response = client.newCall(request)
            .execute()

        val displayName = NextNewsUserAdapter().fromXml(response.body!!.byteStream().konsumeXml())
        response.close()

        return displayName
    }

    suspend fun synchronize(syncType: SyncType, syncData: NextcloudNewsSyncData): SyncResult =
        with(CoroutineScope(Dispatchers.IO)) {
            return if (syncType == SyncType.INITIAL_SYNC) {
                SyncResult().apply {
                    listOf(
                        async { folders = getFolders() },
                        async { feeds = getFeeds() },
                        async { items = getItems(ItemQueryType.ALL.value, false, MAX_ITEMS) },
                        async {
                            starredItems =
                                getItems(ItemQueryType.STARRED.value, true, MAX_STARRED_ITEMS)
                        }
                    ).awaitAll()
                }
            } else {
                listOf(
                    async { setItemsReadState(syncData) },
                    async { setItemsStarState(syncData) },
                ).awaitAll()

                SyncResult().apply {
                    listOf(
                        async { folders = getFolders() },
                        async { feeds = getFeeds() },
                        async { items = getNewItems(syncData.lastModified, ItemQueryType.ALL) }
                    ).awaitAll()
                }
            }
        }

    suspend fun getFolders() = service.getFolders()

    suspend fun getFeeds() = service.getFeeds()

    suspend fun getItems(type: Int, read: Boolean, batchSize: Int): List<Item> {
        return service.getItems(type, read, batchSize)
    }

    suspend fun getNewItems(lastModified: Long, itemQueryType: ItemQueryType): List<Item> {
        return service.getNewItems(lastModified, itemQueryType.value)
    }

    suspend fun createFeed(url: String, folderId: Int?): List<Feed> {
        return service.createFeed(url, folderId)
    }

    suspend fun changeFeedFolder(newFolderId: Int?, feedId: Int) {
        service.changeFeedFolder(feedId, mapOf("folderId" to newFolderId))
    }

    suspend fun renameFeed(name: String, feedId: Int) {
        service.renameFeed(feedId, mapOf("feedTitle" to name))
    }

    suspend fun deleteFeed(feedId: Int) {
        service.deleteFeed(feedId)
    }

    suspend fun createFolder(name: String): List<Folder> {
        return service.createFolder(mapOf("name" to name))
    }

    suspend fun renameFolder(name: String, folderId: Int) {
        service.renameFolder(folderId, mapOf("name" to name))
    }

    suspend fun deleteFolder(folderId: Int) {
        service.deleteFolder(folderId)
    }

    suspend fun setItemsReadState(syncData: NextcloudNewsSyncData) = with(syncData) {
        if (unreadIds.isNotEmpty()) {
            service.setReadState(
                StateType.UNREAD.name.lowercase(),
                mapOf("itemIds" to unreadIds)
            )
        }

        if (readIds.isNotEmpty()) {
            service.setReadState(
                StateType.READ.name.lowercase(),
                mapOf("itemIds" to readIds)
            )
        }
    }

    suspend fun setItemsStarState(syncData: NextcloudNewsSyncData) = with(syncData) {
        if (starredIds.isNotEmpty()) {
            service.setStarState(
                StateType.STAR.name.lowercase(),
                mapOf("itemIds" to starredIds)
            )
        }

        if (unstarredIds.isNotEmpty()) {
            service.setStarState(
                StateType.UNSTAR.name.lowercase(),
                mapOf("itemIds" to unstarredIds)
            )
        }
    }

    enum class ItemQueryType(val value: Int) {
        ALL(3),
        STARRED(2)
    }

    enum class StateType {
        READ,
        UNREAD,
        STAR,
        UNSTAR
    }

    companion object {
        private const val MAX_ITEMS = 5000
        private const val MAX_STARRED_ITEMS = 1000
    }
}