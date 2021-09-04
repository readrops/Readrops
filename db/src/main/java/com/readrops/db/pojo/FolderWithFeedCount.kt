package com.readrops.db.pojo

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.readrops.db.entities.Folder

data class FolderWithFeedCount(
        @Embedded val folder: Folder,
        @ColumnInfo(name = "feed_count") val feedCount: Int,
)