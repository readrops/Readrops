package com.readrops.readropslibrary.services;

import androidx.annotation.NonNull;

import com.readrops.readropslibrary.utils.HttpManager;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Abstraction level for services APIs
 *
 * @param <T> an API service interface
 */
public abstract class API<T> {

    protected T api;

    public API(Credentials credentials, @NonNull Class<T> clazz, @NonNull String endPoint) {
        buildAPI(credentials, clazz, endPoint);
    }

    protected Retrofit getConfiguredRetrofitInstance(@NonNull HttpManager httpManager, @NonNull String endPoint) {
        return new Retrofit.Builder()
                .baseUrl(httpManager.getCredentials().getUrl() + endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpManager.getOkHttpClient())
                .build();
    }

    private T createAPI(@NonNull Credentials credentials, @NonNull Class<T> clazz, @NonNull String endPoint) {
        HttpManager httpManager = new HttpManager(credentials);
        Retrofit retrofit = getConfiguredRetrofitInstance(httpManager, endPoint);

        return retrofit.create(clazz);
    }

    public void buildAPI(@NonNull Credentials credentials, @NonNull Class<T> clazz, @NonNull String endPoint) {
        api = createAPI(credentials, clazz, endPoint);
    }
}
