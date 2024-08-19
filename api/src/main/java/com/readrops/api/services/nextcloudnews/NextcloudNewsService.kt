package com.readrops.api.services.nextcloudnews

import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NextcloudNewsService {

    @GET("folders")
    suspend fun getFolders(): List<Folder>

    @GET("feeds")
    suspend fun getFeeds(): List<Feed>

    @GET("items")
    suspend fun getItems(
        @Query("type") type: Int,
        @Query("getRead") read: Boolean,
        @Query("batchSize") batchSize: Int
    ): List<Item>

    @GET("items/updated")
    suspend fun getNewItems(
        @Query("lastModified") lastModified: Long,
        @Query("type") type: Int
    ): List<Item>

    @POST("items/{stateType}/multiple")
    @JvmSuppressWildcards
    suspend fun setReadState(
        @Path("stateType") stateType: String,
        @Body itemIdsMap: Map<String, List<Int>>
    )

    @POST("items/{starType}/multiple")
    @JvmSuppressWildcards
    suspend fun setStarState(
        @Path("starType") starType: String?,
        @Body body: Map<String, List<Int>>
    )

    @POST("feeds")
    suspend fun createFeed(@Query("url") url: String, @Query("folderId") folderId: Int?): List<Feed>

    @DELETE("feeds/{feedId}")
    suspend fun deleteFeed(@Path("feedId") feedId: Int)

    @POST("feeds/{feedId}/move")
    suspend fun changeFeedFolder(@Path("feedId") feedId: Int, @Body folderIdMap: Map<String, Int?>)

    @POST("feeds/{feedId}/rename")
    suspend fun renameFeed(@Path("feedId") feedId: Int, @Body feedTitleMap: Map<String, String>)

    @POST("folders")
    suspend fun createFolder(@Body folderName: Map<String, String>): List<Folder>

    @DELETE("folders/{folderId}")
    suspend fun deleteFolder(@Path("folderId") folderId: Int)

    @PUT("folders/{folderId}")
    suspend fun renameFolder(@Path("folderId") folderId: Int, @Body folderName: Map<String, String>)

    companion object {
        const val END_POINT = "/index.php/apps/news/api/v1-3/"
    }
}