package com.readrops.api.services.freshrss

import com.readrops.api.services.freshrss.adapters.FreshRSSUserInfo
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FreshRSSService {

    @POST("accounts/ClientLogin")
    suspend fun login(@Body body: RequestBody?): ResponseBody

    @GET("reader/api/0/token")
    suspend fun getWriteToken(): ResponseBody

    @GET("reader/api/0/user-info")
    suspend fun userInfo(): FreshRSSUserInfo

    @GET("reader/api/0/subscription/list?output=json")
    suspend fun getFeeds(): List<Feed>

    @GET("reader/api/0/tag/list?output=json")
    suspend fun getFolders(): List<Folder>

    @GET("reader/api/0/stream/contents/user/-/state/com.google/reading-list")
    suspend fun getItems(
        @Query("xt") excludeTarget: List<String>?,
        @Query("n") max: Int,
        @Query("ot") lastModified: Long?
    ): List<Item>

    @GET("reader/api/0/stream/contents/user/-/state/com.google/starred")
    suspend fun getStarredItems(@Query("n") max: Int): List<Item>

    @GET("reader/api/0/stream/items/ids")
    suspend fun getItemsIds(
        @Query("xt") excludeTarget: String?,
        @Query("s") includeTarget: String?,
        @Query("n") max: Int
    ): List<String>

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    suspend fun setItemsState(
        @Field("T") token: String,
        @Field("a") addAction: String?,
        @Field("r") removeAction: String?,
        @Field("i") itemIds: List<String>
    )

    @FormUrlEncoded
    @POST("reader/api/0/subscription/edit")
    suspend fun createOrDeleteFeed(
        @Field("T") token: String,
        @Field("s") feedUrl: String,
        @Field("ac") action: String,
        @Field("a") folderId: String?
    )

    @FormUrlEncoded
    @POST("reader/api/0/subscription/edit")
    suspend fun updateFeed(
        @Field("T") token: String,
        @Field("s") feedUrl: String,
        @Field("t") title: String,
        @Field("a") folderId: String,
        @Field("ac") action: String
    )

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    suspend fun createFolder(@Field("T") token: String, @Field("a") tagName: String)

    @FormUrlEncoded
    @POST("reader/api/0/rename-tag")
    suspend fun updateFolder(
        @Field("T") token: String,
        @Field("s") folderId: String,
        @Field("dest") newFolderId: String
    )

    @FormUrlEncoded
    @POST("reader/api/0/disable-tag")
    suspend fun deleteFolder(@Field("T") token: String, @Field("s") folderId: String)

    companion object {
        const val END_POINT = "api/greader.php/"
    }
}