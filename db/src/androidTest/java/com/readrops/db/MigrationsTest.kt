package com.readrops.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
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

    @Test
    fun migrate3to4() {
        helper.createDatabase(dbName, 3).apply {
            execSQL("Insert Into Account(account_type, last_modified, current_account, notifications_enabled) Values(0, 0, 0, 0)")
            execSQL("Insert Into Feed(text_color, background_color, account_id, notification_enabled) Values(0, 0, 3, 0)")
            execSQL("Insert Into Item(title, feed_id, read_time, read, starred, read_it_later, guid) values(\"test\", 12, 0, 0, 0, 0, \"guid\")")
        }

        helper.runMigrationsAndValidate(dbName, 4, true, MigrationFrom3To4).apply {
            val remoteId = compileStatement("Select remote_id From Item").simpleQueryForString()
            assertEquals("guid", remoteId)
        }
    }
}