package com.readrops.api.localfeed.atom

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

class ATOMFeedAdapter : XmlAdapter<Pair<Feed, List<Item>>> {

    override fun fromXml(konsumer: Konsumer): Pair<Feed, List<Item>> {
        val feed = Feed()

        val items = arrayListOf<Item>()
        val itemAdapter = ATOMItemAdapter()

        return try {
            konsumer.checkElement(LocalRSSHelper.ATOM_ROOT_NAME) {
                it.allChildrenAutoIgnore(names) {
                    with(feed) {
                        when (tagName) {
                            "title" -> name = nonNullText()
                            "link" -> parseLink(this@allChildrenAutoIgnore, feed)
                            "subtitle" -> description = nullableText()
                            "logo" -> imageUrl = nullableText()
                            "entry" -> items += itemAdapter.fromXml(this@allChildrenAutoIgnore)
                            else -> skipContents()
                        }
                    }
                }
            }

            konsumer.close()
            Pair(feed, items)
        } catch (e: Exception) {
            throw ParseException("ATOM feed parsing failure", e)
        }
    }

    private fun parseLink(konsumer: Konsumer, feed: Feed) = with(konsumer) {
        val rel = attributes.getValueOrNull("rel")

        if (rel == "self")
            feed.url = attributes["href"]
        else if (rel == "alternate")
            feed.siteUrl = attributes["href"]
    }

    companion object {
        val names = Names.of("title", "link", "subtitle", "logo", "entry")
    }
}