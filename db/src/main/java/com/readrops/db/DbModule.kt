package com.readrops.db

import androidx.room.Room
import org.koin.dsl.module

val dbModule = module {

    single(createdAtStart = true) {
        Room.databaseBuilder(get(), Database::class.java, "readrops-db")
            .addMigrations(MigrationFrom1To2, MigrationFrom2To3, MigrationFrom3To4)
            .build()
    }
}