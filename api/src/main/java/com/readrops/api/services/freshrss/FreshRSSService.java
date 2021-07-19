package com.readrops.api.services.freshrss;

import com.readrops.api.services.freshrss.json.FreshRSSUserInfo;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.Item;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FreshRSSService {

    String END_POINT = "/api/greader.php/";

    @POST("accounts/ClientLogin")
    Single<ResponseBody> login(@Body RequestBody body);

    @GET("reader/api/0/token")
    Single<ResponseBody> getWriteToken();

    @GET("reader/api/0/user-info")
    Single<FreshRSSUserInfo> getUserInfo();

    @GET("reader/api/0/subscription/list?output=json")
    Single<List<Feed>> getFeeds();

    @GET("reader/api/0/stream/contents/user/-/state/com.google/reading-list")
    Single<List<Item>> getItems(@Query("xt") List<String> excludeTarget, @Query("n") int max, @Query("ot") Long lastModified);

    @GET("reader/api/0/stream/contents/user/-/state/com.google/starred")
    Single<List<Item>> getStarredItems(@Query("n") int max);

    @GET("reader/api/0/stream/items/ids")
    Single<List<String>> getItemsIds(@Query("xt") String excludeTarget, @Query("s") String includeTarget, @Query("n") int max);

    @GET("reader/api/0/tag/list?output=json")
    Single<List<Folder>> getFolders();

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    Completable setItemsState(@Field("T") String token, @Field("a") String addAction, @Field("r") String removeAction, @Field("i") List<String> itemIds);

    @FormUrlEncoded
    @POST("reader/api/0/subscription/edit")
    Completable createOrDeleteFeed(@Field("T") String token, @Field("s") String feedUrl, @Field("ac") String action);

    @FormUrlEncoded
    @POST("reader/api/0/subscription/edit")
    Completable updateFeed(@Field("T") String token, @Field("s") String feedUrl, @Field("t") String title, @Field("a") String folderId, @Field("ac") String action);

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    Completable createFolder(@Field("T") String token, @Field("a") String tagName);

    @FormUrlEncoded
    @POST("reader/api/0/rename-tag")
    Completable updateFolder(@Field("T") String token, @Field("s") String folderId, @Field("dest") String newFolderId);

    @FormUrlEncoded
    @POST("reader/api/0/disable-tag")
    Completable deleteFolder(@Field("T") String token, @Field("s") String folderId);
}
