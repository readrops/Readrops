package com.readrops.app.compose

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.readrops.api.apiModule
import com.readrops.db.dbModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

open class ReadropsApp : Application(), KoinComponent, ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ReadropsApp)

            modules(apiModule, dbModule, composeAppModule)
        }

        createNotificationChannels()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient { get() }
            .crossfade(true)
            .build()
    }

    // TODO check each channel usefulness
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