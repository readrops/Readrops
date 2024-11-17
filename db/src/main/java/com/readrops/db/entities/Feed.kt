package com.readrops.db.entities

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.readrops.db.entities.account.Account

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL
        ), ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Feed(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String? = null,
    var description: String? = null,
    var url: String? = null,
    @ColumnInfo("image_url") var imageUrl: String? = null,
    var siteUrl: String? = null,
    @ColumnInfo("last_updated") var lastUpdated: String? = null,
    @ColorInt var color: Int = 0,
    @ColumnInfo(name = "icon_url") var iconUrl: String? = null,
    var etag: String? = null,
    @ColumnInfo(name = "last_modified") var lastModified: String? = null,
    @ColumnInfo(name = "folder_id", index = true) var folderId: Int? = null,
    @ColumnInfo("remote_id") var remoteId: String? = null,
    @ColumnInfo(name = "account_id", index = true) var accountId: Int = 0,
    @ColumnInfo(
        name = "notification_enabled",
        defaultValue = "1"
    ) var isNotificationEnabled: Boolean = true,
    @Ignore var unreadCount: Int = 0,
    @Ignore var remoteFolderId: String? = null,
)