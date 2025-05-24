package com.readrops.app

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import coil3.ColorImage
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.test.FakeImageLoaderEngine
import coil3.util.DebugLogger
import coil3.util.Logger

class TestApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()

        /*startKoin {
            androidLogger(Level.INFO)
            androidContext(this@TestApplication)

            modules(
                module {
                    single {
                        Room.inMemoryDatabaseBuilder(this@TestApplication, Database::class.java)
                            .build()
                    }
                },
                apiModule, appModule
            )
        }*/
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val fakeEngine = FakeImageLoaderEngine.Builder()
            .default(ColorImage(Color.Companion.Blue.toArgb(), width = 300, height = 300))
            .build()

        return ImageLoader.Builder(this)
            .logger(DebugLogger(minLevel = Logger.Level.Debug))
            .components { add(fakeEngine) }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .build()
            }
            .build()
    }
}