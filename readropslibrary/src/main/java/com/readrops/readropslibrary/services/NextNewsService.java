package com.readrops.readropslibrary.services;

import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeeds;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolders;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItems;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NextNewsService {

    String ENDPOINT = "/index.php/apps/news/api/v1-2/";

    @GET("folders")
    Call<NextNewsFolders> getFolders();

    @GET("feeds")
    Call<NextNewsFeeds> getFeeds();

    @GET("items")
    Call<NextNewsItems> getItems(@Query("type") int type, @Query("getRead") boolean read, @Query("batchSize") int batchSize);

    @GET("items/updated?type=3")
    Call<NextNewsItems> getNewItems(@Query("lastModified") long lastModified);

    @PUT("items/read/multiple")
    Call<ResponseBody> setReadArticles(@Body List<Integer> itemsIds);

    @PUT("items/unread/multiple")
    Call<ResponseBody> setUnreadArticles(@Body List<Integer> itemsIds);

    @PUT("items/starred/multiple")
    Call<ResponseBody> setStarredArticles(@Body List<Integer> itemsIds);

    @PUT("items/unstarred/multiple")
    Call<ResponseBody> setUnstarredArticles(@Body List<Integer> itemsIds);

    @PUT("items/{stateType}/multiple")
    Call<ResponseBody> setArticlesState(@Path("stateType") String stateType, @Body List<Integer> items);
}
