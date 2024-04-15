package com.readrops.db.pojo

import android.os.Parcelable
import androidx.room.Embedded
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedWithFolder(
    @Embedded(prefix = "feed_") val feed: Feed,
    @Embedded(prefix = "folder_") val folder: Folder,
) : Parcelable

data class FolderWithFeed(
    val folderId: Int,
    val folderName: String,
    val feedId: Int = 0,
    val feedName: String? = null,
    val feedIcon: String? = null,
    val feedUrl: String? = null,
    val feedDescription: String? = null,
    val feedSiteUrl: String? = null,
    val unreadCount: Int = 0,
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