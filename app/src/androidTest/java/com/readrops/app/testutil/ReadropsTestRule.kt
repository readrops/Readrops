package com.readrops.app.testutil

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.readrops.api.apiModule
import com.readrops.app.appModule
import com.readrops.db.Database
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

@OptIn(KoinInternalApi::class)
class ReadropsTestRule : TestWatcher() {

    private var _koin: Koin? = null
    val koin: Koin
        get() = _koin ?: error("No Koin application found")

    override fun starting(description: Description?) {
        closeExistingInstance()
        _koin = startKoin {
            androidLogger(Level.INFO)
            androidContext(ApplicationProvider.getApplicationContext<Context>())

            modules(
                module {
                    single {
                        Room.inMemoryDatabaseBuilder(get(), Database::class.java)
                            .build()
                    }
                },
                apiModule, appModule
            )
        }.koin

        koin.logger.info("Koin Rule - starting")
    }

    private fun closeExistingInstance() {
        KoinPlatformTools.defaultContext().getOrNull()?.let { koin ->
            koin.logger.info("Koin Rule - closing existing instance")
            koin.close()
        }
    }

    override fun finished(description: Description?) {
        koin.logger.info("Koin Rule - finished")
        stopKoin()
        _koin = null
    }
}