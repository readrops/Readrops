package com.readrops.readropslibrary.utils;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.readrops.readropslibrary.BuildConfig;
import com.readrops.readropslibrary.services.Credentials;

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

    public HttpManager(final Credentials credentials) {
        this.credentials = credentials;

        buildOkHttp();
    }

    private void buildOkHttp() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .callTimeout(30, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.HOURS);

        httpBuilder.addInterceptor(new AuthInterceptor());

        if (BuildConfig.DEBUG) {
            StethoInterceptor loggingInterceptor = new StethoInterceptor();
            httpBuilder.addNetworkInterceptor(loggingInterceptor);
        }

        okHttpClient = httpBuilder.build();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setCredentials(Credentials credentials) {
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

    public class AuthInterceptor implements Interceptor {

        public AuthInterceptor() {
            // empty constructor
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (credentials.getAuthorization() != null) {
                request = request.newBuilder()
                        .addHeader("Authorization", credentials.getAuthorization())
                        .build();
            }

            return chain.proceed(request);
        }
    }
}
