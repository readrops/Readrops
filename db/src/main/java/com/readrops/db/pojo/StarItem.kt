package com.readrops.db.pojo

import androidx.room.ColumnInfo


data class StarItem(@ColumnInfo val feedRemoteId: String,
        @ColumnInfo(name = "guid") val guidHash: String)