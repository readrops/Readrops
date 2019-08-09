package com.readrops.readropslibrary.utils;

import com.readrops.readropslibrary.services.Credentials;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpManager {

    private OkHttpClient okHttpClient;
    private Credentials credentials;

    public HttpManager(final Credentials credentials) {
        this.credentials = credentials;

        if (credentials.getAuthorization() != null) {
            okHttpClient = HttpBuilder.getBuilder()
                    .addInterceptor(new AuthInterceptor())
                    .build();
        } else {
            okHttpClient = HttpBuilder.getBuilder()
                    .build();
        }

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
