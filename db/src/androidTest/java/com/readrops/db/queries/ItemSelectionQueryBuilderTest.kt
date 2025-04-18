package com.readrops.db.queries

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.readrops.db.Database
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItemSelectionQueryBuilderTest {

    private lateinit var database: Database

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun defaultCaseTest() {
        val query = ItemSelectionQueryBuilder.buildQuery(10, separateState = false)
        database.query(query)

        with(query.sql) {
            assertTrue(contains("Item.id = 10"))
            assertFalse(contains("Left Join ItemState"))
        }
    }

    @Test
    fun separateStateTest() {
        val query = ItemSelectionQueryBuilder.buildQuery(10, separateState = true)
        database.query(query)

        with(query.sql) {
            assertTrue(contains("Left Join ItemState"))
            assertTrue(contains("ItemState.read"))
            assertTrue(contains("ItemState.starred"))
        }
    }
}