package com.readrops.readropslibrary.localfeed;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class RSSNetwork {

    public void request(String url, okhttp3.Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        okHttpClient.newCall(request).enqueue(callback);
    }


}
