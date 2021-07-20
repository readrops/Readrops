package com.readrops.api.services.freshrss

import com.readrops.api.services.freshrss.adapters.FreshRSSUserInfo
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface FreshRSSService {

    @POST("accounts/ClientLogin")
    fun login(@Body body: RequestBody?): Single<ResponseBody?>?

    @get:GET("reader/api/0/token")
    val writeToken: Single<ResponseBody>

    @get:GET("reader/api/0/user-info")
    val userInfo: Single<FreshRSSUserInfo>

    @get:GET("reader/api/0/subscription/list?output=json")
    val feeds: Single<List<Feed>>

    @get:GET("reader/api/0/tag/list?output=json")
    val folders: Single<List<Folder>>

    @GET("reader/api/0/stream/contents/user/-/state/com.google/reading-list")
    fun getItems(@Query("xt") excludeTarget: List<String>?, @Query("n") max: Int,
                 @Query("ot") lastModified: Long?): Single<List<Item>>

    @GET("reader/api/0/stream/contents/user/-/state/com.google/starred")
    fun getStarredItems(@Query("n") max: Int): Single<List<Item>>

    @GET("reader/api/0/stream/items/ids")
    fun getItemsIds(@Query("xt") excludeTarget: String?, @Query("s") includeTarget: String?,
                    @Query("n") max: Int): Single<List<String>>

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    fun setItemsState(@Field("T") token: String, @Field("a") addAction: String?,
                      @Field("r") removeAction: String?, @Field("i") itemIds: List<String>): Completable

    @FormUrlEncoded
    @POST("reader/api/0/subscription/edit")
    fun createOrDeleteFeed(@Field("T") token: String, @Field("s") feedUrl: String, @Field("ac") action: String): Completable

    @FormUrlEncoded
    @POST("reader/api/0/subscription/edit")
    fun updateFeed(@Field("T") token: String, @Field("s") feedUrl: String, @Field("t") title: String,
                   @Field("a") folderId: String, @Field("ac") action: String): Completable

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    fun createFolder(@Field("T") token: String, @Field("a") tagName: String): Completable

    @FormUrlEncoded
    @POST("reader/api/0/rename-tag")
    fun updateFolder(@Field("T") token: String, @Field("s") folderId: String, @Field("dest") newFolderId: String): Completable

    @FormUrlEncoded
    @POST("reader/api/0/disable-tag")
    fun deleteFolder(@Field("T") token: String, @Field("s") folderId: String): Completable

    companion object {
        const val END_POINT = "/api/greader.php/"
    }
}