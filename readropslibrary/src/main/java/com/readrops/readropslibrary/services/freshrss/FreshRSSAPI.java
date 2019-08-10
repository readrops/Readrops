package com.readrops.readropslibrary.services.freshrss;

import androidx.annotation.NonNull;

import com.readrops.readropslibrary.services.API;
import com.readrops.readropslibrary.services.SyncType;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFeeds;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItems;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSUserInfo;

import java.io.StringReader;
import java.util.Properties;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FreshRSSAPI extends API<FreshRSSService> {

    public FreshRSSAPI(FreshRSSCredentials credentials) {
        super(credentials, FreshRSSService.class, FreshRSSService.END_POINT);
    }

    /**
     * Call token API to generate a new token from account credentials
     *
     * @param login
     * @param password
     * @return the generated token
     */
    public Single<String> login(@NonNull String login, @NonNull String password) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Email", login)
                .addFormDataPart("Passwd", password)
                .build();

        return api.login(requestBody)
                .flatMap(response -> {
                    Properties properties = new Properties();
                    properties.load(new StringReader(response.string()));

                    return Single.just(properties.getProperty("Auth"));
                });
    }

    public Single<FreshRSSUserInfo> getUserInfo() {
        return api.getUserInfo();
    }

    public Single<FreshRSSSyncResult> sync(@NonNull SyncType syncType, @NonNull FreshRSSSyncData syncData) {
        FreshRSSSyncResult syncResult = new FreshRSSSyncResult();

        return getFeeds()
                .flatMap(freshRSSFeeds -> {
                    syncResult.setFeeds(freshRSSFeeds.getSubscriptions());

                    switch (syncType) {
                        case INITIAL_SYNC:
                            return getItems(EXCLUDE_ITEMS.EXCLUDE_READ_ITEMS.value, 10000, null);
                        case CLASSIC_SYNC:
                            return getItems(EXCLUDE_ITEMS.EXCLUDE_READ_ITEMS.value, 10000, syncData.getLastModified());
                    }

                    return Single.error(new Exception("Unknown sync type"));
                })
                .flatMap(freshRSSItems -> {
                    syncResult.setItems(freshRSSItems.getItems());

                    return Single.just(syncResult);
                });
    }

    public Single<FreshRSSFeeds> getFeeds() {
        return api.getFeeds();
    }

    public Single<FreshRSSItems> getItems(String excludeTarget, Integer max, Long lastModified) {
        return api.getItems(excludeTarget, max, lastModified);
    }

    public enum EXCLUDE_ITEMS {
        EXCLUDE_READ_ITEMS("user/-/state/com.google/read");

        String value;

        EXCLUDE_ITEMS(String value) {
            this.value = value;
        }
    }
}
