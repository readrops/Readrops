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

    protected static final int MAX_ITEMS = 5000;

    protected T api;
    private Retrofit retrofit;

    private Class<T> clazz;
    private String endPoint;

    public API(Credentials credentials, @NonNull Class<T> clazz, @NonNull String endPoint) {
        this.clazz = clazz;
        this.endPoint = endPoint;

        api = createAPI(credentials);
    }

    protected Retrofit getConfiguredRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(HttpManager.getInstance().getCredentials().getUrl() + endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(HttpManager.getInstance().getOkHttpClient())
                .build();
    }

    private T createAPI(@NonNull Credentials credentials) {
        HttpManager.getInstance().setCredentials(credentials);
        retrofit = getConfiguredRetrofitInstance();

        return retrofit.create(clazz);
    }

    public void setCredentials(@NonNull Credentials credentials) {
        HttpManager.getInstance().setCredentials(credentials);

        retrofit = retrofit.newBuilder()
                .baseUrl(credentials.getUrl() + endPoint)
                .build();

        api = retrofit.create(clazz);
    }
}
