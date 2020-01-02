package com.readrops.app;

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
import com.readrops.readropslibrary.utils.HttpManager;

public class ReadropsDebugApp extends ReadropsApp {

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, false);

        initFlipper();
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
}

