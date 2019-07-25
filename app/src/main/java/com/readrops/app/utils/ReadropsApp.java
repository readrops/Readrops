package com.readrops.app.utils;

import android.app.Application;

import io.reactivex.plugins.RxJavaPlugins;

public class ReadropsApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RxJavaPlugins.setErrorHandler(e -> { });
    }
}
