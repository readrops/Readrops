package com.readrops.readropslibrary.services.freshrss;

import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItems;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSUserInfo;

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
    Single<FreshRSSItems> getItems(@Query("xt") String excludeTarget, @Query("n") int max, @Query("ot") Long lastModified);

    @GET("reader/api/0/tag/list?output=json")
    Single<List<Folder>> getFolders();

    @FormUrlEncoded
    @POST("reader/api/0/edit-tag")
    Completable setItemsReadState(@Field("T") String token, @Field("a") String readAction, @Field("r") String unreadAction, @Field("i") List<String> itemIds);

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
