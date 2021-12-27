package com.readrops.api.services.fever

import com.readrops.api.services.fever.adapters.Favicon
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
    suspend fun getFeeds(@Body body: MultipartBody): List<Feed>

    @POST("?groups")
    suspend fun getFolders(@Body body: MultipartBody): List<Folder>

    @POST("?favicons")
    suspend fun getFavicons(@Body body: MultipartBody): List<Favicon>

    @POST("?items")
    suspend fun getItems(@Body body: MultipartBody): List<Item>

    @POST("?unread_item_ids")
    suspend fun getUnreadItemsIds(@Body body: MultipartBody): List<String>

    @POST("?saved_item_ids")
    suspend fun getStarredItemsIds(@Body body: MultipartBody): List<String>
}