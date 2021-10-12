package com.readrops.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.readrops.api.apiModule
import com.readrops.app.utils.SharedPreferencesManager
import com.readrops.db.dbModule
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

open class ReadropsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        RxJavaPlugins.setErrorHandler { e: Throwable? -> }

        createNotificationChannels()
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ReadropsApp)

            modules(apiModule, dbModule, appModule)
        }

        val theme = when (SharedPreferencesManager.readString(SharedPreferencesManager.SharedPrefKey.DARK_THEME)) {
            getString(R.string.theme_value_light) -> AppCompatDelegate.MODE_NIGHT_NO
            getString(R.string.theme_value_dark) -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(theme)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val feedsColorsChannel = NotificationChannel(FEEDS_COLORS_CHANNEL_ID,
                    getString(R.string.feeds_colors), NotificationManager.IMPORTANCE_DEFAULT)
            feedsColorsChannel.description = getString(R.string.get_feeds_colors)

            val opmlExportChannel = NotificationChannel(OPML_EXPORT_CHANNEL_ID,
                    getString(R.string.opml_export), NotificationManager.IMPORTANCE_DEFAULT)
            opmlExportChannel.description = getString(R.string.opml_export_description)

            val syncChannel = NotificationChannel(SYNC_CHANNEL_ID,
                    getString(R.string.auto_synchro), NotificationManager.IMPORTANCE_LOW)
            syncChannel.description = getString(R.string.account_synchro)

            val manager = getSystemService(NotificationManager::class.java)!!

            manager.createNotificationChannel(feedsColorsChannel)
            manager.createNotificationChannel(opmlExportChannel)
            manager.createNotificationChannel(syncChannel)
        }
    }

    companion object {
        const val FEEDS_COLORS_CHANNEL_ID = "feedsColorsChannel"
        const val OPML_EXPORT_CHANNEL_ID = "opmlExportChannel"
        const val SYNC_CHANNEL_ID = "syncChannel"
    }

}