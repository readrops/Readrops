package com.readrops.db.pojo

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import kotlinx.parcelize.Parcelize

@Parcelize //TODO delete
data class FeedWithFolder(
    @Embedded(prefix = "feed_") val feed: Feed,
    @Embedded(prefix = "folder_") val folder: Folder,
) : Parcelable

data class FeedWithFolder2(
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
    val feedUrl: String? = null,
    val feedDescription: String? = null,
    val feedSiteUrl: String? = null,
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