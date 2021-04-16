package com.readrops.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.readrops.db.entities.account.Account

@Entity(foreignKeys = [ForeignKey(entity = Account::class, parentColumns = ["id"],
        childColumns = ["account_id"], onDelete = ForeignKey.CASCADE)])
data class UnreadItemsIds(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo(name = "remote_id", index = true) val remoteId: String,
        @ColumnInfo(name = "account_id", index = true) val accountId: Int,
)

@Entity
data class ReadStarStateChange(
        @PrimaryKey val id: Int = 0,
        @ColumnInfo(name = "read_change") val readChange: Boolean = false,
        @ColumnInfo(name = "star_change") val starChange: Boolean = false,
        @ColumnInfo(name = "account_id") val accountId: Int,
)