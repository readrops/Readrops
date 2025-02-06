package com.readrops.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.readrops.db.entities.account.Account

@Entity(
    foreignKeys = [ForeignKey(
        entity = Account::class, parentColumns = ["id"],
        childColumns = ["account_id"], onDelete = ForeignKey.CASCADE
    )]
)
data class Folder(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String? = null,
    var remoteId: String? = null,
    @ColumnInfo(name = "account_id", index = true) var accountId: Int = 0,
    @Ignore var unreadCount: Int = 0,
) : Comparable<Folder> {

    override fun compareTo(other: Folder): Int = this.name!!.compareTo(other.name!!)
}

data class FolderWithFeedAndUnreadCount(
    @Embedded val folder: Folder?,
    @Relation(parentColumn = "id", entityColumn = "folder_id")
    val feeds: List<FeedAndUnreadCount>
)

fun List<FolderWithFeedAndUnreadCount>.unbox(): FoldersWithFeedAndUnreadCount {
    var totalUnreadCount = 0
    val resultFoldersAndFeeds = this.associate { folderAndFeeds ->
        var unreadCount = 0
        val resultFeeds = folderAndFeeds.feeds.map {
            unreadCount += it.unreadCount
            totalUnreadCount += it.unreadCount
            it.feed.copy(unreadCount = it.unreadCount)
        }
        folderAndFeeds.folder?.copy(unreadCount = unreadCount) to resultFeeds
    }
    return FoldersWithFeedAndUnreadCount(
        totalUnreadCount = totalUnreadCount,
        foldersAndFeeds = resultFoldersAndFeeds
    )
}

data class FoldersWithFeedAndUnreadCount(
    val totalUnreadCount: Int,
    val foldersAndFeeds: Map<Folder?, List<Feed>>
)