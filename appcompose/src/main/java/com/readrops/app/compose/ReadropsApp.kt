package com.readrops.app.compose

import android.app.Application
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
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient { get() }
            .crossfade(true)
            .build()
    }
}