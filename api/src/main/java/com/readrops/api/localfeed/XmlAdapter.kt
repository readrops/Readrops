package com.readrops.api.localfeed

import com.readrops.api.localfeed.atom.ATOMFeedAdapter
import com.readrops.api.localfeed.atom.ATOMItemsAdapter
import com.readrops.api.localfeed.rss2.RSS2FeedAdapter
import com.readrops.api.localfeed.rss2.RSS2ItemsAdapter
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import java.io.InputStream
import java.lang.IllegalArgumentException

interface XmlAdapter<T> {

    fun fromXml(inputStream: InputStream): T

    companion object {
        fun xmlFeedAdapterFactory(type: LocalRSSHelper.RSSType): XmlAdapter<Feed> {
            return when (type) {
                LocalRSSHelper.RSSType.RSS_2 -> RSS2FeedAdapter()
                LocalRSSHelper.RSSType.ATOM -> ATOMFeedAdapter()
                else -> throw IllegalArgumentException("Unknown RSS type : $type")
            }
        }

        fun xmlItemsAdapterFactory(type: LocalRSSHelper.RSSType): XmlAdapter<List<Item>> {
            return when (type) {
                LocalRSSHelper.RSSType.RSS_2 -> RSS2ItemsAdapter()
                LocalRSSHelper.RSSType.ATOM -> ATOMItemsAdapter()
                else -> throw IllegalArgumentException("Unknown RSS type : $type")
            }
        }
    }
}

