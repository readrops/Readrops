package com.readrops.readropslibrary.utils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public final class HttpBuilder {

    private static OkHttpClient.Builder builder;

    public static OkHttpClient.Builder getBuilder() {
        if (builder == null)
            builder = createOkHttpBuilder();

        return builder;
    }

    private static OkHttpClient.Builder createOkHttpBuilder() {
        return new OkHttpClient.Builder();
    }
}
