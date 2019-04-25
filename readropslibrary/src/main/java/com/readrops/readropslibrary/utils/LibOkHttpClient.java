package com.readrops.readropslibrary.utils;

import okhttp3.OkHttpClient;

public final class LibOkHttpClient {

    private static OkHttpClient okhttpClient;

    public static OkHttpClient getInstance() {
        if (okhttpClient == null)
            okhttpClient = createOkHttpInstance();

        return okhttpClient;
    }

    private static OkHttpClient createOkHttpInstance() {
        return new OkHttpClient.Builder()
                .build();
    }
}
