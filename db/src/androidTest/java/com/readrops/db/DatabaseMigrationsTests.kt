package com.readrops.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationsTests {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            "com.readrops.readropsdb.Database/",
            FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        helper.createDatabase(testDb, 1).close()
        helper.runMigrationsAndValidate(testDb, 2, true, Database.MIGRATION_1_2).close()
    }
}