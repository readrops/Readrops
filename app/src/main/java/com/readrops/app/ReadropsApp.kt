package com.readrops.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
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

            modules(apiModule, dbModule, appModule)
        }

        createNotificationChannels()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient { get() }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val syncChannel = NotificationChannel(
                SYNC_CHANNEL_ID,
                getString(R.string.auto_synchro),
                NotificationManager.IMPORTANCE_LOW
            )
            syncChannel.description = getString(R.string.account_synchro)

            NotificationManagerCompat.from(this)
                .createNotificationChannel(syncChannel)
        }
    }

    companion object {
        const val SYNC_CHANNEL_ID = "syncChannel"
    }
}