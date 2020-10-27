package com.readrops.db

import androidx.room.Room
import org.koin.dsl.module

val dbModule = module {

    single(createdAtStart = true) {
        Room.databaseBuilder(get(), Database::class.java, "readrops-db")
                .addMigrations(Database.MIGRATION_1_2)
                .addMigrations(Database.MIGRATION_2_3)
                .build()
    }
}