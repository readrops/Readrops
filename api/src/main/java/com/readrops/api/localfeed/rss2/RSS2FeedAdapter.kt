package com.readrops.api.localfeed.rss2

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
import org.jsoup.Jsoup

class RSS2FeedAdapter : XmlAdapter<Pair<Feed, List<Item>>> {

    override fun fromXml(konsumer: Konsumer): Pair<Feed, List<Item>> {
        val feed = Feed()

        val items = arrayListOf<Item>()
        val itemAdapter = RSS2ItemAdapter()

        return try {
            konsumer.checkElement(LocalRSSHelper.RSS_2_ROOT_NAME) {
                it.child("channel") {
                    allChildrenAutoIgnore(names) {
                        with(feed) {
                            when (tagName) {
                                "title" -> name = Jsoup.parse(nonNullText()).text()
                                "description" -> description = nullableText()
                                "link" -> siteUrl = nullableText()
                                "atom:link" -> {
                                    if (attributes.getValueOrNull("rel") == "self")
                                        url = attributes.getValueOrNull("href")
                                }
                                "item" -> items += itemAdapter.fromXml(this@allChildrenAutoIgnore)
                                "image" -> imageUrl = parseImage(this@allChildrenAutoIgnore)
                                else -> skipContents()
                            }
                        }
                    }
                }
            }

            konsumer.close()
            Pair(feed, items)
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseImage(konsumer: Konsumer): String? = with(konsumer) {
        var url: String? = null

        allChildrenAutoIgnore(Names.of("url")) {
            when (tagName) {
                "url" -> url = nullableText()
                else -> skipContents()
            }
        }

        url
    }

    companion object {
        val names = Names.of("title", "description", "link", "item", "image")
    }
}