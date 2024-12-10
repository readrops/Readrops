package com.readrops.db.pojo

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item
import com.readrops.db.entities.OpenIn

data class ItemWithFeed(
    @Embedded val item: Item,
    @ColumnInfo(name = "name") val feedName: String,
    @ColumnInfo(name = "feedId") val feedId: Int,
    @ColumnInfo(name = "color") @ColorInt val color: Int,
    @ColumnInfo(name = "icon_url") val feedIconUrl: String?,
    @ColumnInfo(name = "siteUrl") val websiteUrl: String?,
    @Embedded(prefix = "folder_") val folder: Folder?,
    // duplicates of Item.isRead and Item.isStarred
    // this is a workaround to make the UI refresh itself when updating read/star state
    // as since kotlin 2.0.20 it wouldn't work anymore, because of Item properties mutability
    // TODO see how to resolve this by improving Item immutability
    @ColumnInfo(name = "is_starred") val isStarred: Boolean = false,
    @ColumnInfo(name = "is_read") val isRead: Boolean = false,
    @ColumnInfo(name = "open_in") val openIn: OpenIn?
)