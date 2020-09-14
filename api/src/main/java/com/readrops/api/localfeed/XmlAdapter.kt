package com.readrops.api.localfeed

import com.readrops.api.localfeed.atom.ATOMFeedAdapter
import com.readrops.api.localfeed.rss.RSSFeedAdapter
import com.readrops.api.localfeed.rss.RSSItemsAdapter
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import java.io.InputStream

interface XmlAdapter<T> {

    fun fromXml(inputStream: InputStream): T

    companion object {
        fun xmlFeedAdapterFactory(type: LocalRSSHelper.RSSType): XmlAdapter<Feed> {
            return when (type) {
                LocalRSSHelper.RSSType.RSS_2 -> RSSFeedAdapter()
                LocalRSSHelper.RSSType.ATOM -> ATOMFeedAdapter()
                else -> throw Exception("Unknown RSS type : $type")
            }
        }

        fun xmlItemsAdapterFactory(type: LocalRSSHelper.RSSType): XmlAdapter<List<Item>> {
            return when (type) {
                LocalRSSHelper.RSSType.RSS_2 -> RSSItemsAdapter()
                else -> throw Exception("Unknown RSS type : $type")
            }
        }
    }
}

