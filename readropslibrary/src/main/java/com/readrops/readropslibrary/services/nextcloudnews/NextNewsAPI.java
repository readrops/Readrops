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

    public SyncResults sync(@NonNull Credentials credentials, @NonNull SyncType syncType, @Nullable SyncData data) throws IOException {
        HttpManager httpManager = new HttpManager(credentials);
        Retrofit retrofit = getConfiguredRetrofitInstance(httpManager);

        NextNewsService api = retrofit.create(NextNewsService.class);

        TimingLogger timings = new TimingLogger(TAG, "sync");

        NextNewsFeeds feedList = api.getFeeds().execute().body();
        timings.addSplit("get feeds");

        NextNewsFolders folderList = api.getFolders().execute().body();
        timings.addSplit("get folders");

        NextNewsItems itemList = api.getItems(3, false, 300).execute().body();
        timings.addSplit("get items");
        timings.dumpToLog();

        SyncResults results = new SyncResults();
        results.setFeeds(feedList.getFeeds());
        results.setFolders(folderList.getFolders());
        results.setItems(itemList.getItems());

        return results;
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
