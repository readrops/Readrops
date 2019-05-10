package com.readrops.readropslibrary.services;

import androidx.annotation.CallSuper;

import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeed;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeeds;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolders;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItems;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NextNewsService {

    String ENDPOINT = "/index.php/apps/news/api/v1-2/";

    @GET("folders")
    Call<NextNewsFolders> getFolders();

    @GET("feeds")
    Call<NextNewsFeeds> getFeeds();

    @GET("items?type={type}&getRead={read}&batchSize={batchSize}")
    Call<NextNewsItems> getItems(@Path("type") int type, @Path("read") boolean read, @Path("batchSize") int batchSize);

    @GET("items/updated?lastModified={lastModified}&type=3")
    Call<NextNewsItems> getNewItems(@Path("lastModified") long lastModified);

    @PUT("items/read/multiple")
    Call<ResponseBody> setReadArticles();

    @PUT("items/unread/multiple")
    Call<ResponseBody> setUnreadArticles();

    @PUT("items/starred/multiple")
    Call<ResponseBody> setStarredArticles();

    @PUT("items/unstarred/multiple")
    Call<ResponseBody> setUnstarredArticles();
}
