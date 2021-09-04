package com.readrops.api.localfeed.rss1

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.readrops.api.localfeed.LocalRSSHelper
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.checkElement
import com.readrops.api.utils.extensions.nonNullText
import com.readrops.api.utils.extensions.nullableText
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item

class RSS1FeedAdapter : XmlAdapter<Pair<Feed, List<Item>>> {

    override fun fromXml(konsumer: Konsumer): Pair<Feed, List<Item>> {
        val feed = Feed()

        val items = arrayListOf<Item>()
        val itemAdapter = RSS1ItemAdapter()

        return try {
            konsumer.checkElement(LocalRSSHelper.RSS_1_ROOT_NAME) {
                it.allChildrenAutoIgnore(Names.of("channel", "item")) {
                    when (tagName) {
                        "channel" -> parseChannel(this, feed)
                        "item" -> items += itemAdapter.fromXml(this)
                    }
                }
            }

            konsumer.close()
            Pair(feed, items)
        } catch (e: Exception) {
            throw ParseException(e.message)
        }

    }

    private fun parseChannel(konsumer: Konsumer, feed: Feed) = with(konsumer) {
        feed.url = attributes.getValueOrNull("about",
                namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")

        allChildrenAutoIgnore(names) {
            with(feed) {
                when (tagName) {
                    "title" -> name = nonNullText()
                    "link" -> siteUrl = nonNullText()
                    "description" -> description = nullableText()
                }
            }
        }
    }

    companion object {
        val names = Names.of("title", "link", "description")
    }
}