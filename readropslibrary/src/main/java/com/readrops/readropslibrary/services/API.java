package com.readrops.readropslibrary.services;

import androidx.annotation.NonNull;

import com.readrops.readropslibrary.utils.HttpManager;
import com.squareup.moshi.Moshi;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Abstraction level for services APIs
 *
 * @param <T> an API service interface
 */
public abstract class API<T> {

    protected static final int MAX_ITEMS = 5000;

    protected T api;

    public API(Credentials credentials, @NonNull Class<T> clazz, @NonNull String endPoint) {
        api = createAPI(credentials, clazz, endPoint);
    }

    protected abstract Moshi buildMoshi();

    protected Retrofit getConfiguredRetrofitInstance(@NonNull String endPoint) {
        return new Retrofit.Builder()
                .baseUrl(HttpManager.getInstance().getCredentials().getUrl() + endPoint)
                .addConverterFactory(MoshiConverterFactory.create(buildMoshi()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(HttpManager.getInstance().getOkHttpClient())
                .build();
    }

    private T createAPI(@NonNull Credentials credentials, @NonNull Class<T> clazz, @NonNull String endPoint) {
        HttpManager.getInstance().setCredentials(credentials);
        Retrofit retrofit = getConfiguredRetrofitInstance(endPoint);

        return retrofit.create(clazz);
    }

    public void setCredentials(@NonNull Credentials credentials) {
        HttpManager.getInstance().setCredentials(credentials);
    }
}
