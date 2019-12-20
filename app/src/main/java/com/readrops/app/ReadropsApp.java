package com.readrops.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.facebook.stetho.Stetho;
import com.readrops.app.utils.SharedPreferencesManager;

import io.reactivex.plugins.RxJavaPlugins;

public class ReadropsApp extends Application {

    public static final String FEEDS_COLORS_CHANNEL_ID = "feedsColorsChannel";
    public static final String OPML_EXPORT_CHANNEL_ID = "opmlExportChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        RxJavaPlugins.setErrorHandler(e -> {
        });

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        createNotificationChannels();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (Boolean.valueOf(SharedPreferencesManager.readString(this, SharedPreferencesManager.SharedPrefKey.DARK_THEME)))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel feedsColorsChannel = new NotificationChannel(FEEDS_COLORS_CHANNEL_ID,
                    getString(R.string.feeds_colors), NotificationManager.IMPORTANCE_DEFAULT);
            feedsColorsChannel.setDescription(getString(R.string.get_feeds_colors));

            NotificationChannel opmlExportChannel = new NotificationChannel(OPML_EXPORT_CHANNEL_ID,
                    getString(R.string.opml_export), NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(feedsColorsChannel);
            manager.createNotificationChannel(opmlExportChannel);
        }
    }
}
