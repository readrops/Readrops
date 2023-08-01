package com.readrops.api.services.freshrss

import com.readrops.api.services.freshrss.adapters.FreshRSSUserInfo
import com.readrops.db.entities.Item
import okhttp3.MultipartBody
import java.io.StringReader
import java.util.Properties

class NewFreshRSSDataSource(private val service: NewFreshRSSService) {

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

    suspend fun sync() {

    }

    suspend fun getFolders() = service.getFolders()

    suspend fun getFeeds() = service.getFeeds()

    suspend fun getItems(excludeTargets: List<String>, max: Int, lastModified: Long): List<Item> {
        return service.getItems(excludeTargets, max, lastModified)
    }

    suspend fun getStarredItems(max: Int) = service.getStarredItems(max)

    suspend fun getItemsIds(excludeTarget: String, includeTarget: String, max: Int): List<String> {
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

    suspend fun createFeed(token: String, feedUrl: String) {
        service.createOrDeleteFeed(token, FEED_PREFIX + feedUrl, "subscribe");
    }

    suspend fun deleteFeed(token: String, feedUrl: String) {
        service.createOrDeleteFeed(token, FEED_PREFIX + feedUrl, "unsubscribe")
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

    suspend fun setItemsReadState(syncData: FreshRSSSyncData, token: String) {
        if (syncData.readItemsIds.isNotEmpty()) {
            setItemsReadState(true, syncData.readItemsIds, token)
        }

        if (syncData.unreadItemsIds.isNotEmpty()) {
            setItemsReadState(false, syncData.unreadItemsIds, token)
        }
    }

    suspend fun setItemsStarState(syncData: FreshRSSSyncData, token: String) {
        if (syncData.starredItemsIds.isNotEmpty()) {
            setItemStarState(true, syncData.starredItemsIds, token)
        }

        if (syncData.unstarredItemsIds.isNotEmpty()) {
            setItemStarState(false, syncData.unstarredItemsIds, token)
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