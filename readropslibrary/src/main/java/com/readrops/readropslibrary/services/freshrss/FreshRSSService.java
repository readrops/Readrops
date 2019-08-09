package com.readrops.readropslibrary.services.freshrss;

import com.readrops.readropslibrary.services.freshrss.json.FreshRSSUserInfo;

import io.reactivex.Single;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface FreshRSSService {

    String END_POINT = "/api/greader.php/";

    @POST("accounts/ClientLogin")
    Single<ResponseBody> login(@Body RequestBody body);

    @GET("reader/api/0/user-info")
    Single<FreshRSSUserInfo> getUserInfo();

}
