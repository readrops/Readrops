package com.readrops.api.services.fever

import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeverService {

    @POST("?api")
    suspend fun login(@Body body: MultipartBody): ResponseBody

    @POST("?feeds")
    suspend fun getFeeds(): List<Feed>

    @get:POST("?groups")
    val folders: List<Folder>

    @get:GET("?favicons")
    val favicons: Unit

    @POST("?items")
    suspend fun items(): List<Item>

    @POST("?unread_item_ids")
    suspend fun unreadItemsIds(): List<String>

    @POST("?saved_item_ids")
    suspend fun starredItemsIds()

    @get:GET("?api")
    val api: Unit

    companion object {
        const val END_POINT = ""
    }
}