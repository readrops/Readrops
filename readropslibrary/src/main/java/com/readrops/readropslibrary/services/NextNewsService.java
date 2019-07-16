package com.readrops.readropslibrary.services;

import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeeds;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolder;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolders;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItemIds;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItems;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsUser;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NextNewsService {

    String ENDPOINT = "/index.php/apps/news/api/v1-2/";

    @GET("user")
    Call<NextNewsUser> getUser();

    @GET("folders")
    Call<NextNewsFolders> getFolders();

    @GET("feeds")
    Call<NextNewsFeeds> getFeeds();

    @GET("items")
    Call<NextNewsItems> getItems(@Query("type") int type, @Query("getRead") boolean read, @Query("batchSize") int batchSize);

    @GET("items/updated")
    Call<NextNewsItems> getNewItems(@Query("lastModified") long lastModified, @Query("type") int type);

    @PUT("items/{stateType}/multiple")
    Call<ResponseBody> setArticlesState(@Path("stateType") String stateType, @Body NextNewsItemIds items);

    @POST("feeds")
    Call<NextNewsFeeds> createFeed(@Query("url") String url, @Query("folderId") int folderId);

    @POST("folders")
    Call<NextNewsFolders> createFolder(@Body NextNewsFolder folder);

    @DELETE("folders/{folderId}")
    Call<ResponseBody> deleteFolder(@Path("folderId") int folderId);

    @PUT("folders/{folderId}")
    Call<ResponseBody> renameFolder(@Path("folderId") int folderId, @Body NextNewsFolder folder);
}
