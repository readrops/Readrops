package com.readrops.readropslibrary.services.nextcloudnews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.readropslibrary.services.NextNewsService;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeeds;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolders;
import com.readrops.readropslibrary.utils.HttpManager;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NextNewsAPI {

    public NextNewsAPI() {

    }

    private Retrofit getConfiguredRetrofitInstance(@NonNull HttpManager httpManager) {
        return new Retrofit.Builder()
                .baseUrl(httpManager.getCredentials().getUrl() + NextNewsService.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpManager.getOkHttpClient())
                .build();
    }

    public void sync(@NonNull Credentials credentials, @NonNull SyncType syncType, @Nullable SyncData data) throws IOException {
        HttpManager httpManager = new HttpManager(credentials);
        Retrofit retrofit = getConfiguredRetrofitInstance(httpManager);

        NextNewsService api = retrofit.create(NextNewsService.class);
    }

    public enum SyncType {
        INITIAL_SYNC,
        CLASSIC_SYNC
    }
}
