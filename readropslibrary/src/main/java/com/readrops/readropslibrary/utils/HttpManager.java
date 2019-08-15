package com.readrops.readropslibrary.utils;

import com.readrops.readropslibrary.BuildConfig;
import com.readrops.readropslibrary.services.Credentials;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpManager {

    private OkHttpClient okHttpClient;
    private Credentials credentials;

    public HttpManager(final Credentials credentials) {
        this.credentials = credentials;

        OkHttpClient.Builder httpBuilder = HttpBuilder.getBuilder();

        if (credentials.getAuthorization() != null)
            httpBuilder.addInterceptor(new AuthInterceptor());

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            httpBuilder.addInterceptor(loggingInterceptor);
        }

        okHttpClient = httpBuilder.build();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public class AuthInterceptor implements Interceptor {

        public AuthInterceptor() {
            // empty constructor
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            // AuthInterceptor is called twice when using Okhttp for the second time and I don't know why,
            // which adds the authorization header twice and make the auth fail
            // So preventively, I delete the first added header
            // TODO : find why AuthInterceptor is called twice
            request = request.newBuilder().removeHeader("Authorization")
                    .addHeader("Authorization", credentials.getAuthorization())
                    .build();

            return chain.proceed(request);
        }
    }
}
