package com.readrops.db.pojo

import android.os.Parcelable
import androidx.room.Embedded
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedWithFolder(
        @Embedded(prefix = "feed_") val feed: Feed,
        @Embedded(prefix = "folder_") val folder: Folder?,
) : Parcelable