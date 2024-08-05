package com.readrops.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Feed::class,
            parentColumns = ["id"],
            childColumns = ["feed_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String? = null,
    var description: String? = null,
    @ColumnInfo(name = "clean_description") var cleanDescription: String? = null,
    var link: String? = null,
    @ColumnInfo(name = "image_link") var imageLink: String? = null,
    var author: String? = null,
    @ColumnInfo(name = "pub_date") var pubDate: LocalDateTime? = null,
    var content: String? = null,
    @ColumnInfo(name = "feed_id", index = true) var feedId: Int = 0,
    @ColumnInfo(name = "read_time") var readTime: Double = 0.0,
    @ColumnInfo(name = "read") var isRead: Boolean = false,
    @ColumnInfo(name = "starred") var isStarred: Boolean = false,
    @ColumnInfo(name = "remote_id") var remoteId: String? = null,
    @Ignore var feedRemoteId: String? = null,
) : Comparable<Item> {

    val text
        get() = if (content != null) content else description

    val hasImage
        get() = imageLink != null

    override fun compareTo(other: Item): Int = this.pubDate!!.compareTo(other.pubDate)
}