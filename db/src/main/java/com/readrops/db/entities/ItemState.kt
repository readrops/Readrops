package com.readrops.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.readrops.db.entities.account.Account

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItemStateChange(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "read_change") val readChange: Boolean = false,
    @ColumnInfo(name = "star_change") val starChange: Boolean = false,
    @ColumnInfo(name = "account_id") val accountId: Int,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index( // TODO check potential performance regression at insertion when synchronizing
        value = ["remote_id", "account_id"],
        unique = true
    )]
)
data class ItemState(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val read: Boolean = false,
    val starred: Boolean = false,
    @ColumnInfo(name = "remote_id") val remoteId: String,
    @ColumnInfo(name = "account_id") val accountId: Int,
)