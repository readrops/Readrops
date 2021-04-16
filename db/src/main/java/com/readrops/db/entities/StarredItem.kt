package com.readrops.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore

@Entity(foreignKeys = [ForeignKey(entity = Feed::class, parentColumns = ["id"], childColumns = ["feed_id"],
        onDelete = ForeignKey.CASCADE)], inheritSuperIndices = true)
class StarredItem() : Item() {

    // TODO really hacky, should be replaced by something better
    @Ignore
    constructor(item: Item) : this() {
        id = item.id
        title = item.title
        description = item.description
        cleanDescription = item.cleanDescription
        link = item.link
        imageLink = item.imageLink
        author = item.author
        pubDate = item.pubDate
        content = item.content
        feedId = item.feedId
        guid = item.guid
        readTime = item.readTime
        isRead = item.isRead
        isStarred = true // important here for the items query compatibility
        isReadItLater = item.isReadItLater
        remoteId = item.remoteId
        feedRemoteId = item.feedRemoteId
    }

}