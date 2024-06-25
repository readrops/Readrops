package com.readrops.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationsTest {

    private val dbName = "TEST-DB"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        Database::class.java
    )

    @Test
    fun migrate1To2() {
        helper.createDatabase(dbName, 1).apply {
            close()
        }

        helper.runMigrationsAndValidate(dbName, 2, true, MigrationFrom1To2).apply {
            close()
        }
    }

    @Test
    fun migrate2to3() {
        helper.createDatabase(dbName, 2).apply {
            close()
        }

        helper.runMigrationsAndValidate(dbName, 3, true, MigrationFrom2To3).apply {
            close()
        }
    }
}