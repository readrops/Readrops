package com.readrops.readropslibrary.services.nextcloudnews;

import android.util.TimingLogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.readropslibrary.services.NextNewsService;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeeds;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolders;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItems;
import com.readrops.readropslibrary.utils.HttpManager;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NextNewsAPI {

    private static final String TAG = NextNewsAPI.class.getSimpleName();

    public NextNewsAPI() {

    }

    private Retrofit getConfiguredRetrofitInstance(@NonNull HttpManager httpManager) {
        return new Retrofit.Builder()
                .baseUrl(httpManager.getCredentials().getUrl() + NextNewsService.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpManager.getOkHttpClient())
                .build();
    }

    public SyncResult sync(@NonNull Credentials credentials, @NonNull SyncType syncType, @Nullable SyncData data) throws IOException {
        HttpManager httpManager = new HttpManager(credentials);
        Retrofit retrofit = getConfiguredRetrofitInstance(httpManager);

        NextNewsService api = retrofit.create(NextNewsService.class);

        SyncResult syncResult = new SyncResult();
        TimingLogger timings = new TimingLogger(TAG, "sync");

        Response<NextNewsFeeds> feedResponse = api.getFeeds().execute();
        NextNewsFeeds feedList = feedResponse.body();
        timings.addSplit("get feeds");

        if (!feedResponse.isSuccessful())
            syncResult.setError(true);

        Response<NextNewsFolders> folderResponse = api.getFolders().execute();
        NextNewsFolders folderList = folderResponse.body();
        timings.addSplit("get folders");

        if (!folderResponse.isSuccessful())
            syncResult.setError(true);

        Response<NextNewsItems> itemsResponse = api.getItems(3, false, -1).execute();
        NextNewsItems itemList = itemsResponse.body();
        timings.addSplit("get items");

        if (!itemsResponse.isSuccessful())
            syncResult.setError(true);

        timings.dumpToLog();

        if (feedList.getFeeds() != null)
            syncResult.setFeeds(feedList.getFeeds());

        if (folderList.getFolders() != null)
            syncResult.setFolders(folderList.getFolders());

        if (itemList.getItems() != null)
            syncResult.setItems(itemList.getItems());

        return syncResult;
    }

    public enum SyncType {
        INITIAL_SYNC,
        CLASSIC_SYNC
    }

    public enum StateType {
        READ,
        UNREAD,
        STARRED,
        UNSTARRED
    }
}
