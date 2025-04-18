package com.readrops.api.services.greader

import com.readrops.api.services.DataSourceResult
import com.readrops.api.services.SyncType
import com.readrops.api.services.greader.adapters.FreshRSSUserInfo
import com.readrops.db.entities.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import okhttp3.MultipartBody
import java.io.StringReader
import java.util.Properties

class GReaderDataSource(private val service: GReaderService) {

    suspend fun login(login: String, password: String): String {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("Email", login)
            .addFormDataPart("Passwd", password)
            .build()

        val response = service.login(requestBody)

        val properties = Properties()
        properties.load(StringReader(response.string()))

        response.close()
        return properties.getProperty("Auth")
    }

    suspend fun getWriteToken(): String = service.getWriteToken().string()

    suspend fun getUserInfo(): FreshRSSUserInfo = service.userInfo()

    suspend fun synchronize(
        syncType: SyncType,
        syncData: GReaderSyncData,
        writeToken: String
    ): DataSourceResult = with(CoroutineScope(Dispatchers.IO)) {
        return if (syncType == SyncType.INITIAL_SYNC) {
            DataSourceResult().apply {
                awaitAll(
                    async { folders = getFolders() },
                    async { feeds = getFeeds() },
                    async {
                        items = getItems(listOf(GOOGLE_READ, GOOGLE_STARRED), MAX_ITEMS, null)
                    },
                    async { starredItems = getStarredItems(MAX_STARRED_ITEMS) },
                    async { unreadIds = getItemsIds(GOOGLE_READ, GOOGLE_READING_LIST, MAX_ITEMS) },
                    async { starredIds = getItemsIds(null, GOOGLE_STARRED, MAX_STARRED_ITEMS) }
                )

            }
        } else {
            DataSourceResult().apply {
                awaitAll(
                    async { setItemsReadState(syncData, writeToken) },
                    async { setItemsStarState(syncData, writeToken) },
                )

                awaitAll(
                    async { folders = getFolders() },
                    async { feeds = getFeeds() },
                    async { items = getItems(null, MAX_ITEMS, syncData.lastModified) },
                    async { unreadIds = getItemsIds(GOOGLE_READ, GOOGLE_READING_LIST, MAX_ITEMS) },
                    async {
                        readIds = getItemsIds(GOOGLE_UNREAD, GOOGLE_READING_LIST, MAX_ITEMS)
                    },
                    async { starredIds = getItemsIds(null, GOOGLE_STARRED, MAX_STARRED_ITEMS) }
                )
            }
        }

    }

    suspend fun getFolders() = service.getFolders()

    suspend fun getFeeds() = service.getFeeds()

    suspend fun getItems(excludeTargets: List<String>?, max: Int, lastModified: Long?): List<Item> {
        return service.getItems(excludeTargets, max, lastModified)
    }

    suspend fun getStarredItems(max: Int) = service.getStarredItems(max)

    suspend fun getItemsIds(excludeTarget: String?, includeTarget: String, max: Int): List<String> {
        return service.getItemsIds(excludeTarget, includeTarget, max)
    }

    private suspend fun setItemsReadState(read: Boolean, itemIds: List<String>, token: String) {
        return if (read) {
            service.setItemsState(token, GOOGLE_READ, null, itemIds)
        } else {
            service.setItemsState(token, null, GOOGLE_READ, itemIds)
        }
    }

    private suspend fun setItemStarState(starred: Boolean, itemIds: List<String>, token: String) {
        return if (starred) {
            service.setItemsState(token, GOOGLE_STARRED, null, itemIds)
        } else {
            service.setItemsState(token, null, GOOGLE_STARRED, itemIds)
        }
    }

    suspend fun createFeed(token: String, feedUrl: String, folderId: String?) {
        // no feed here of the folder prefix for the folder id
        service.createOrDeleteFeed(token, FEED_PREFIX + feedUrl, "subscribe", folderId)
    }

    suspend fun deleteFeed(token: String, feedUrl: String) {
        service.createOrDeleteFeed(token, FEED_PREFIX + feedUrl, "unsubscribe", null)
    }

    suspend fun updateFeed(token: String, feedUrl: String, title: String, folderId: String) {
        service.updateFeed(token, FEED_PREFIX + feedUrl, title, folderId, "edit")
    }

    suspend fun createFolder(token: String, tagName: String) {
        service.createFolder(token, "$FOLDER_PREFIX$tagName")
    }

    suspend fun updateFolder(token: String, folderId: String, name: String) {
        service.updateFolder(token, folderId, "$FOLDER_PREFIX$name")
    }

    suspend fun deleteFolder(token: String, folderId: String) {
        service.deleteFolder(token, folderId)
    }

    private suspend fun setItemsReadState(syncData: GReaderSyncData, token: String) {
        if (syncData.readIds.isNotEmpty()) {
            setItemsReadState(true, syncData.readIds, token)
        }

        if (syncData.unreadIds.isNotEmpty()) {
            setItemsReadState(false, syncData.unreadIds, token)
        }
    }

    private suspend fun setItemsStarState(syncData: GReaderSyncData, token: String) {
        if (syncData.starredIds.isNotEmpty()) {
            setItemStarState(true, syncData.starredIds, token)
        }

        if (syncData.unstarredIds.isNotEmpty()) {
            setItemStarState(false, syncData.unstarredIds, token)
        }
    }

    companion object {
        private const val MAX_ITEMS = 2500
        private const val MAX_STARRED_ITEMS = 1000

        const val GOOGLE_READ = "user/-/state/com.google/read"
        const val GOOGLE_UNREAD = "user/-/state/com.google/unread"
        const val GOOGLE_STARRED = "user/-/state/com.google/starred"
        const val GOOGLE_READING_LIST = "user/-/state/com.google/reading-list"

        const val FEED_PREFIX = "feed/"
        const val FOLDER_PREFIX = "user/-/label/"
    }
}