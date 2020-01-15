package com.readrops.readropslibrary.services.nextcloudnews;

import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropsdb.entities.Item;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsUser;

import java.util.List;
import java.util.Map;

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

    String END_POINT = "/index.php/apps/news/api/v1-2/";

    @GET("user")
    Call<NextNewsUser> getUser();

    @GET("folders")
    Call<List<Folder>> getFolders();

    @GET("feeds")
    Call<List<Feed>> getFeeds();

    @GET("items")
    Call<List<Item>> getItems(@Query("type") int type, @Query("getRead") boolean read, @Query("batchSize") int batchSize);

    @GET("items/updated")
    Call<List<Item>> getNewItems(@Query("lastModified") long lastModified, @Query("type") int type);

    @PUT("items/{stateType}/multiple")
    Call<ResponseBody> setArticlesState(@Path("stateType") String stateType, @Body Map<String, List<String>> itemIdsMap);

    @POST("feeds")
    Call<List<Feed>> createFeed(@Query("url") String url, @Query("folderId") int folderId);

    @DELETE("feeds/{feedId}")
    Call<Void> deleteFeed(@Path("feedId") int feedId);

    @PUT("feeds/{feedId}/move")
    Call<ResponseBody> changeFeedFolder(@Path("feedId") int feedId, @Body Map<String, Integer> folderIdMap);

    @PUT("feeds/{feedId}/rename")
    Call<ResponseBody> renameFeed(@Path("feedId") int feedId, @Body Map<String, String> feedTitleMap);

    @POST("folders")
    Call<List<Folder>> createFolder(@Body Map<String, String> folderName);

    @DELETE("folders/{folderId}")
    Call<ResponseBody> deleteFolder(@Path("folderId") int folderId);

    @PUT("folders/{folderId}")
    Call<ResponseBody> renameFolder(@Path("folderId") int folderId, @Body Map<String, String> folderName);
}
