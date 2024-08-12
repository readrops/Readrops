package com.readrops.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.readrops.api.apiModule
import com.readrops.app.util.CrashActivity
import com.readrops.app.util.FeverFaviconFetcher
import com.readrops.db.dbModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import kotlin.system.exitProcess

open class ReadropsApp : Application(), KoinComponent, ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val intent = Intent(this, CrashActivity::class.java).apply {
                putExtra(CrashActivity.THROWABLE_KEY, throwable)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }

            startActivity(intent)
            exitProcess(0)
        }

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
            .components { add(FeverFaviconFetcher.Factory(get())) }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maximumMaxSizeBytes(1024 * 1024 * 100)
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