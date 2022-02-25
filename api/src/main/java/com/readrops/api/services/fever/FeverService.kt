package com.readrops.api.services.fever

import com.readrops.api.services.fever.adapters.Favicon
import com.readrops.api.services.fever.adapters.FeverFeeds
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface FeverService {

    @POST("?api")
    suspend fun login(@Body body: MultipartBody): ResponseBody

    @POST("?feeds")
    suspend fun getFeeds(@Body body: MultipartBody): FeverFeeds

    @POST("?groups")
    suspend fun getFolders(@Body body: MultipartBody): List<Folder>

    @POST("?favicons")
    suspend fun getFavicons(@Body body: MultipartBody): List<Favicon>

    @POST("?items")
    suspend fun getItems(@Body body: MultipartBody, @Query("max_id") maxId: String?,
                         @Query("since_id") sinceId: String?): List<Item>

    @POST("?unread_item_ids")
    suspend fun getUnreadItemsIds(@Body body: MultipartBody): List<String>

    @POST("?saved_item_ids")
    suspend fun getStarredItemsIds(@Body body: MultipartBody): List<String>

    @POST("?mark=item")
    suspend fun updateItemState(@Body body: MultipartBody, @Query("as") action: String,
                                @Query("id") id: String)

}