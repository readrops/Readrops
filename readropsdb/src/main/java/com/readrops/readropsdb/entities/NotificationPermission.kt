package com.readrops.readropsdb.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Feed::class, parentColumns = ["id"],
        childColumns = ["feedId"], onDelete = ForeignKey.CASCADE)])
data class NotificationPermission(@PrimaryKey(autoGenerate = true) val id: Int,
                                  @ColumnInfo(index = true) val feedId: Int,
                                  val enabled: Boolean)