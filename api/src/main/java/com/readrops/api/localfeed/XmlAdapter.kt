package com.readrops.api.localfeed

import com.gitlab.mvysny.konsumexml.Konsumer
import com.readrops.api.localfeed.rss2.RSS2FeedAdapter
import com.readrops.api.localfeed.rss2.RSS2ItemsAdapter
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item

interface XmlAdapter<T> {

    fun fromXml(konsumer: Konsumer): T

    companion object {
        fun xmlFeedAdapterFactory(type: LocalRSSHelper.RSSType): XmlAdapter<Feed> = when (type) {
            //LocalRSSHelper.RSSType.RSS_1 -> RSS1FeedAdapter()
            LocalRSSHelper.RSSType.RSS_2 -> RSS2FeedAdapter()
            //LocalRSSHelper.RSSType.ATOM -> ATOMFeedAdapter()
            else -> throw IllegalArgumentException("Unknown RSS type : $type")
        }

        fun xmlItemsAdapterFactory(type: LocalRSSHelper.RSSType): XmlAdapter<List<Item>> =
                when (type) {
                    //LocalRSSHelper.RSSType.RSS_1 -> RSS1ItemsAdapter()
                    LocalRSSHelper.RSSType.RSS_2 -> RSS2ItemsAdapter()
                    //LocalRSSHelper.RSSType.ATOM -> ATOMItemAdapter()
                    else -> throw IllegalArgumentException("Unknown RSS type : $type")
                }

        const val AUTHORS_MAX = 4
    }
}

