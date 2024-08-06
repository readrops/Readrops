package com.readrops.db.pojo

import androidx.room.ColumnInfo

data class ItemReadStarState(
        @ColumnInfo(name = "remote_id") val remoteId: String,
        val read: Boolean,
        val starred: Boolean,
        @ColumnInfo(name = "read_change") val readChange: Boolean,
        @ColumnInfo(name = "star_change") val starChange: Boolean,
)