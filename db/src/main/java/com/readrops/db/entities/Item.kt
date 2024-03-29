package com.readrops.db.entities

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import org.joda.time.LocalDateTime

@Parcelize
@Entity(foreignKeys = [ForeignKey(entity = Feed::class, parentColumns = ["id"],
        childColumns = ["feed_id"], onDelete = ForeignKey.CASCADE)])
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
        @ColumnInfo(index = true) var guid: String? = null,
        @ColumnInfo(name = "read_time") var readTime: Double = 0.0,
        @ColumnInfo(name = "read") var isRead: Boolean = false,
        @ColumnInfo(name = "starred") var isStarred: Boolean = false,
        @ColumnInfo(name = "read_it_later") var isReadItLater: Boolean = false,
        var remoteId: String? = null,
        @Ignore var feedRemoteId: String? = null,
) : Parcelable, Comparable<Item> {

    val text
        get() = if (content != null) content else description

    val hasImage
        get() = imageLink != null

    override fun compareTo(other: Item): Int = this.pubDate!!.compareTo(other.pubDate)
}