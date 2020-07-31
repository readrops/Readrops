package com.readrops.api.utils;

import androidx.annotation.Nullable;

import com.readrops.api.services.Credentials;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
                .addInterceptor(new AuthInterceptor())
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

    public class AuthInterceptor implements Interceptor {

        public AuthInterceptor() {
            // empty constructor
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (credentials != null && credentials.getAuthorization() != null) {
                request = request.newBuilder()
                        .addHeader("Authorization", credentials.getAuthorization())
                        .build();
            }

            return chain.proceed(request);
        }
    }
}
