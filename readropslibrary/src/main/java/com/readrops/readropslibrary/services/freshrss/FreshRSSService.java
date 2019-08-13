package com.readrops.readropslibrary.services.freshrss;

import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFeeds;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFolders;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItems;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSUserInfo;

import io.reactivex.Single;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FreshRSSService {

    String END_POINT = "/api/greader.php/";

    @POST("accounts/ClientLogin")
    Single<ResponseBody> login(@Body RequestBody body);

    @GET("reader/api/0/user-info")
    Single<FreshRSSUserInfo> getUserInfo();

    @GET("reader/api/0/subscription/list?output=json")
    Single<FreshRSSFeeds> getFeeds();

    @GET("reader/api/0/stream/contents/user/-/state/com.google/reading-list")
    Single<FreshRSSItems> getItems(@Query("xt") String excludeTarget, @Query("n") Integer max, @Query("ot") Long lastModified);

    @GET("reader/api/0/tag/list?output=json")
    Single<FreshRSSFolders> getFolders();
}
