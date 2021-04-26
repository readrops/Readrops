package com.readrops.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ItemStateChange(
        @PrimaryKey val id: Int = 0,
        @ColumnInfo(name = "read_change") val readChange: Boolean = false,
        @ColumnInfo(name = "star_change") val starChange: Boolean = false,
        @ColumnInfo(name = "account_id") val accountId: Int,
)

@Entity
data class ItemState(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val read: Boolean = false,
        val starred: Boolean = false,
        @ColumnInfo(name = "remote_id") val remoteId: String,
        @ColumnInfo(name = "account_id") val accountId: Int,
)