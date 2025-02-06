package com.readrops.db.entities

import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.readrops.db.entities.account.Account

enum class OpenIn {
    LOCAL_VIEW,
    EXTERNAL_VIEW
}

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
    @ColumnInfo(name = "open_in") var openIn: OpenIn = OpenIn.LOCAL_VIEW,
    @ColumnInfo(name = "open_in_ask", defaultValue = "1") var openInAsk: Boolean = true,
    @Ignore var unreadCount: Int = 0,
    @Ignore var remoteFolderId: String? = null,
)

@DatabaseView("""
    SELECT 
        Account.id as accountId,
        Feed.*,
        CASE
            WHEN Account.type = 'GREADER' OR Account.type = 'FRESHRSS' 
                THEN sum(iif(not ItemState.read, 1, 0))
            ELSE sum(iif(not Item.read, 1, 0))
        END
        AS unreadCount
    FROM Account 
    LEFT JOIN Feed ON Feed.account_id = Account.id
    LEFT JOIN Item ON Item.feed_id = Feed.id
    LEFT JOIN ItemState ON Item.remote_id = ItemState.remote_id
    GROUP BY Feed.id
""")
data class FeedAndUnreadCount(
    val accountId: Int,
    val unreadCount: Int = 0,
    @Embedded val feed: Feed
)
