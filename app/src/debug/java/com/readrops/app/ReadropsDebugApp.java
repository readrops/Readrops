package com.readrops.app;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin;
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.navigation.NavigationFlipperPlugin;
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.soloader.SoLoader;
import com.icapps.niddler.core.AndroidNiddler;
import com.icapps.niddler.interceptor.okhttp.NiddlerOkHttpInterceptor;
import com.readrops.readropslibrary.utils.HttpManager;

public class ReadropsDebugApp extends ReadropsApp implements Configuration.Provider {

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, false);

        initFlipper();
        initNiddler();
    }

    private void initFlipper() {
        if (FlipperUtils.shouldEnableFlipper(this)) {
            FlipperClient client = AndroidFlipperClient.getInstance(this);
            client.addPlugin(new InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()));

            NetworkFlipperPlugin networkPlugin = new NetworkFlipperPlugin();
            client.addPlugin(networkPlugin);

            HttpManager.setInstance(
                    HttpManager.getInstance()
                            .getOkHttpClient()
                            .newBuilder()
                            .addInterceptor(new FlipperOkhttpInterceptor(networkPlugin))
                            .build());

            client.addPlugin(new DatabasesFlipperPlugin(this));
            client.addPlugin(CrashReporterPlugin.getInstance());
            client.addPlugin(NavigationFlipperPlugin.getInstance());
            client.addPlugin(new SharedPreferencesFlipperPlugin(this));

            client.start();
        }
    }

    private void initNiddler() {
        AndroidNiddler niddler = new AndroidNiddler.Builder()
                .setNiddlerInformation(AndroidNiddler.fromApplication(this))
                .setPort(0)
                .setMaxStackTraceSize(10)
                .build();

        niddler.attachToApplication(this);

        HttpManager.setInstance(HttpManager.getInstance().
                getOkHttpClient().
                newBuilder().
                addInterceptor(new NiddlerOkHttpInterceptor(niddler, "default"))
                .build());

        niddler.start();
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .build();
    }
}

