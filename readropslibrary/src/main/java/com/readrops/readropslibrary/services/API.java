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
        api = createAPI(credentials, clazz, endPoint);
    }

    protected Retrofit getConfiguredRetrofitInstance(@NonNull String endPoint) {
        return new Retrofit.Builder()
                .baseUrl(HttpManager.getInstance().getCredentials().getUrl() + endPoint)
                .addConverterFactory(GsonConverterFactory.create())
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
