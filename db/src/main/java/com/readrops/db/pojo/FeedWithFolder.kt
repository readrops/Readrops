package com.readrops.db.pojo

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.readrops.db.entities.Feed

data class FeedWithFolder(
    @Embedded val feed: Feed,
    @ColumnInfo(name = "folder_name") val folderName: String?
)

data class FolderWithFeed(
    val folderId: Int?,
    val folderName: String?,
    val folderRemoteId: String?,
    val feedId: Int = 0,
    val feedName: String? = null,
    val feedIcon: String? = null,
    val feedImage: String? = null,
    val feedUrl: String? = null,
    val feedDescription: String? = null,
    val feedSiteUrl: String? = null,
    val feedNotificationsEnabled: Boolean = true,
    val feedRemoteId: String? = null,
    val accountId: Int = 0
)

data class FeedWithCount(
    val feedId: Int = 0,
    val feedName: String? = null,
    val feedIcon: String? = null,
    val feedUrl: String? = null,
    val feedSiteUrl: String? = null,
    val feedDescription: String? = null,
    val unreadCount: Int = 0,
    val accountId: Int = 0
)