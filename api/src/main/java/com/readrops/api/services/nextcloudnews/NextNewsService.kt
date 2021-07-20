package com.readrops.api.services.nextcloudnews

import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface NextNewsService {

    @GET("/ocs/v1.php/cloud/users/{userId}")
    @Headers("OCS-APIRequest: true")
    fun getUser(@Path("userId") userId: String): Call<ResponseBody>

    @get:GET("folders")
    val folders: Call<List<Folder>>

    @get:GET("feeds")
    val feeds: Call<List<Feed>>

    @GET("items")
    fun getItems(@Query("type") type: Int, @Query("getRead") read: Boolean, @Query("batchSize") batchSize: Int): Call<List<Item>>

    @GET("items/updated")
    fun getNewItems(@Query("lastModified") lastModified: Long, @Query("type") type: Int): Call<List<Item>>

    @PUT("items/{stateType}/multiple")
    fun setReadState(@Path("stateType") stateType: String, @Body itemIdsMap: Map<String, List<String>>): Call<ResponseBody>

    @PUT("items/{starType}/multiple")
    fun setStarState(@Path("starType") starType: String?, @Body body: Map<String?, List<Map<String, String>>>): Call<ResponseBody>

    @POST("feeds")
    fun createFeed(@Query("url") url: String, @Query("folderId") folderId: Int): Call<List<Feed>>

    @DELETE("feeds/{feedId}")
    fun deleteFeed(@Path("feedId") feedId: Int): Call<Void?>?

    @PUT("feeds/{feedId}/move")
    fun changeFeedFolder(@Path("feedId") feedId: Int, @Body folderIdMap: Map<String, Int>): Call<ResponseBody>

    @PUT("feeds/{feedId}/rename")
    fun renameFeed(@Path("feedId") feedId: Int, @Body feedTitleMap: Map<String, String>): Call<ResponseBody>

    @POST("folders")
    fun createFolder(@Body folderName: Map<String, String>): Call<List<Folder>>

    @DELETE("folders/{folderId}")
    fun deleteFolder(@Path("folderId") folderId: Int): Call<ResponseBody>

    @PUT("folders/{folderId}")
    fun renameFolder(@Path("folderId") folderId: Int, @Body folderName: Map<String, String>): Call<ResponseBody>

    companion object {
        const val END_POINT = "/index.php/apps/news/api/v1-2/"
    }
}