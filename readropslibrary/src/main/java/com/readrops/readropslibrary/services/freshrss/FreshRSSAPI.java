package com.readrops.readropslibrary.services.freshrss;

import androidx.annotation.NonNull;

import com.readrops.readropslibrary.services.API;
import com.readrops.readropslibrary.services.Credentials;
import com.readrops.readropslibrary.services.SyncType;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFeeds;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFolders;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItems;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSUserInfo;

import java.io.StringReader;
import java.util.Properties;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FreshRSSAPI extends API<FreshRSSService> {

    public FreshRSSAPI(Credentials credentials) {
        super(credentials, FreshRSSService.class, FreshRSSService.END_POINT);
    }

    /**
     * Call token API to generate a new token from account credentials
     *
     * @param login    login
     * @param password password
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

    public Single<String> getWriteToken() {
        return api.getWriteToken()
                .flatMap(responseBody -> Single.just(responseBody.string()));
    }

    public Single<FreshRSSUserInfo> getUserInfo() {
        return api.getUserInfo();
    }

    public Single<FreshRSSSyncResult> sync(@NonNull SyncType syncType, @NonNull FreshRSSSyncData syncData) {
        FreshRSSSyncResult syncResult = new FreshRSSSyncResult();

        return getFolders()
                .flatMap(freshRSSFolders -> {
                    syncResult.setFolders(freshRSSFolders.getTags());

                    return getFeeds();
                })
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
                    syncResult.setLastUpdated(freshRSSItems.getUpdated());

                    return Single.just(syncResult);
                });
    }

    public Single<FreshRSSFolders> getFolders() {
        return api.getFolders();
    }

    public Single<FreshRSSFeeds> getFeeds() {
        return api.getFeeds();
    }

    public Single<FreshRSSItems> getItems(String excludeTarget, Integer max, Long lastModified) {
        return api.getItems(excludeTarget, max, lastModified);
    }

    public Completable markItemReadUnread(Boolean read, String itemId, String token) {
        if (read)
            return api.setItemReadState(token, EXCLUDE_ITEMS.EXCLUDE_READ_ITEMS.value, null, itemId);
        else
            return api.setItemReadState(token, null, EXCLUDE_ITEMS.EXCLUDE_READ_ITEMS.value, itemId);
    }

    public Completable createFeed(String token, String feedUrl) {
        return api.createOrDeleteFeed(token, "feed/" + feedUrl, "subscribe");
    }

    public Completable deleteFeed(String token, String feedUrl) {
        return api.createOrDeleteFeed(token, "feed/" + feedUrl, "unsubscribe");
    }

    public Completable updateFeed(String token, String feedUrl, String title, String folderId) {
        return api.updateFeed(token, "feed/" + feedUrl, title, folderId, "edit");
    }

    public enum EXCLUDE_ITEMS {
        EXCLUDE_READ_ITEMS("user/-/state/com.google/read");

        String value;

        EXCLUDE_ITEMS(String value) {
            this.value = value;
        }
    }
}
