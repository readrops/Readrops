package com.readrops.readropslibrary.utils;

import com.readrops.readropslibrary.services.nextcloudnews.Credentials;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpManager {

    private OkHttpClient okHttpClient;
    private Credentials credentials;

    public HttpManager(final Credentials credentials) {
        this.credentials = credentials;

        okHttpClient = HttpBuilder.getBuilder()
                .callTimeout(20, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.HOURS)
                .addInterceptor(new AuthInterceptor())
                .build();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public class AuthInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            request = request.newBuilder()
                    .addHeader("Authorization", credentials.getBase64())
                    .build();

            return chain.proceed(request);
        }
    }
}
