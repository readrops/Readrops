package com.readrops.app.utils;

import android.app.Application;

import com.facebook.stetho.Stetho;

import io.reactivex.plugins.RxJavaPlugins;

public class ReadropsApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RxJavaPlugins.setErrorHandler(e -> { });
        Stetho.initializeWithDefaults(this);
    }
}
