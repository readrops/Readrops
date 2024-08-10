package com.readrops.api.services.fever

import com.readrops.api.services.SyncType
import com.readrops.api.services.fever.adapters.FeverAPIAdapter
import com.readrops.api.utils.ApiUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import okhttp3.MultipartBody

class FeverDataSource(private val service: FeverService) {

    suspend fun login(login: String, password: String): Boolean {
        val response = service.login(getFeverRequestBody(login, password))

        val adapter = Moshi.Builder()
            .add(Boolean::class.java, FeverAPIAdapter())
            .build()
            .adapter(Boolean::class.java)

        return adapter.fromJson(response.source())!!
    }

    suspend fun synchronize(
        login: String,
        password: String,
        syncType: SyncType,
        syncData: FeverSyncData
    ): FeverSyncResult = with(CoroutineScope(Dispatchers.IO)) {
        val body = getFeverRequestBody(login, password)

        if (syncType == SyncType.INITIAL_SYNC) {
            return FeverSyncResult().apply {
                listOf(
                    async { feverFeeds = service.getFeeds(body) },
                    async { folders = service.getFolders(body) },
                    async {
                        unreadIds = service.getUnreadItemsIds(body)
                            .reversed()
                            .subList(0, MAX_ITEMS_IDS)

                        var lastId = unreadIds.first()
                        items = buildList {
                            repeat(INITIAL_SYNC_ITEMS_REQUESTS_COUNT) {
                                val newItems = service.getItems(body, lastId, null)

                                lastId = newItems.last().remoteId!!
                                addAll(newItems)
                            }
                        }

                        sinceId = unreadIds.first().toLong()
                    },
                    async { starredIds = service.getStarredItemsIds(body) },
                    async { favicons = listOf() }
                )
                    .awaitAll()
            }


        } else {
            return FeverSyncResult().apply {
                listOf(
                    async { folders = service.getFolders(body) },
                    async { feverFeeds = service.getFeeds(body) },
                    async { unreadIds = service.getUnreadItemsIds(body) },
                    async { starredIds = service.getStarredItemsIds(body) },
                    async { favicons = listOf() },
                    async {
                        items = buildList {
                            var sinceId = syncData.sinceId

                            while (true) {
                                val newItems = service.getItems(body, null, sinceId)

                                if (newItems.isEmpty()) break
                                sinceId = newItems.first().remoteId!!
                                addAll(newItems)
                            }
                        }

                        if (items.isNotEmpty()) items.first().remoteId!!.toLong() else sinceId.toLong()
                    }
                )
                    .awaitAll()
            }
        }
    }

    suspend fun setItemState(login: String, password: String, action: String, id: String) {
        val body = getFeverRequestBody(login, password)

        service.updateItemState(body, action, id)
    }

    private fun getFeverRequestBody(login: String, password: String): MultipartBody {
        val credentials = ApiUtils.md5hash("$login:$password")

        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_key", credentials)
            .build()
    }

    companion object {
        private const val MAX_ITEMS_IDS = 5000
        private const val INITIAL_SYNC_ITEMS_REQUESTS_COUNT = 10
    }
}

sealed class ItemAction(val value: String) {
    sealed class ReadStateAction(value: String) : ItemAction(value) {
        data object ReadAction : ReadStateAction("read")
        data object UnreadAction : ReadStateAction("unread")
    }

    sealed class StarStateAction(value: String) : ItemAction(value) {
        data object StarAction : StarStateAction("saved")
        data object UnstarAction : StarStateAction("unsaved")
    }
}