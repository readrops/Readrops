package com.readrops.db.pojo

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item

data class ItemWithFeed(
    @Embedded val item: Item,
    @ColumnInfo(name = "name") val feedName: String,
    @ColumnInfo(name = "feedId") val feedId: Int,
    @ColumnInfo(name = "color") @ColorInt val color: Int,
    @ColumnInfo(name = "icon_url") val feedIconUrl: String?,
    @ColumnInfo(name = "siteUrl") val websiteUrl: String?,
    @Embedded(prefix = "folder_") val folder: Folder?
)