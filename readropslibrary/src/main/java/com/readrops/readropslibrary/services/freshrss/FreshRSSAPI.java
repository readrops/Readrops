package com.readrops.readropslibrary.services.freshrss;

import com.readrops.readropslibrary.services.API;
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

    public Single<String> login(String login, String password) {
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
}
