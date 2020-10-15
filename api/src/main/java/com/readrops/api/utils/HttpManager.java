package com.readrops.api.utils;

import androidx.annotation.Nullable;

import com.readrops.api.services.Credentials;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class HttpManager {

    private OkHttpClient okHttpClient;
    private Credentials credentials;

    public HttpManager() {
        buildOkHttp();
    }

    private void buildOkHttp() {
        okHttpClient = new OkHttpClient.Builder()
                .callTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.HOURS)
                .build();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setCredentials(@Nullable Credentials credentials) {
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    private static HttpManager instance;

    public static HttpManager getInstance() {
        if (instance == null) {
            instance = new HttpManager();
        }

        return instance;
    }

    public static void setInstance(OkHttpClient client) {
        instance.okHttpClient = client;
    }
}
