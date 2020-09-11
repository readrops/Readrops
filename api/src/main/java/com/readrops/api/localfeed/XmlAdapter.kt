package com.readrops.api.localfeed

import com.readrops.api.localfeed.rss.RSSFeedAdapter
import com.readrops.db.entities.Feed
import java.io.InputStream

interface XmlAdapter<T> {

    fun fromXml(inputStream: InputStream): T

    companion object {
        fun xmlFeedAdapterFactory(type: LocalRSSHelper.RSSType): XmlAdapter<Feed> {
            return when (type) {
                LocalRSSHelper.RSSType.RSS_2 -> RSSFeedAdapter()
                else -> throw Exception("Unknown RSS type : $type")
            }
        }
    }
}

