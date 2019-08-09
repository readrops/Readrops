package com.readrops.readropslibrary.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public final class HttpBuilder {

    private static OkHttpClient.Builder builder;

    public static OkHttpClient.Builder getBuilder() {
        if (builder == null)
            builder = createOkHttpBuilder();

        return builder;
    }

    private static OkHttpClient.Builder createOkHttpBuilder() {
        return new OkHttpClient.Builder()
                .callTimeout(30, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.HOURS);
    }
}
