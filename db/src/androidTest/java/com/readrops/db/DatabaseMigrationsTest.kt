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
class DatabaseMigrationsTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            "com.readrops.db.Database/",
            FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2() {
        helper.createDatabase(testDb, 1).close()
        helper.runMigrationsAndValidate(testDb, 2, true, Database.MIGRATION_1_2).close()
    }

    @Test
    fun migrate2to3() {
        helper.createDatabase(testDb, 2).close()
        helper.runMigrationsAndValidate(testDb, 3, true, Database.MIGRATION_2_3).close()
    }
}