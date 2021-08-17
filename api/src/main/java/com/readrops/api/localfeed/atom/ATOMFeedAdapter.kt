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

class ATOMFeedAdapter : XmlAdapter<Feed> {

    override fun fromXml(konsumer: Konsumer): Feed {
        val feed = Feed()

        return try {
            konsumer.checkElement(LocalRSSHelper.ATOM_ROOT_NAME) {
                it.allChildrenAutoIgnore(names) {
                    with(feed) {
                        when (tagName) {
                            "title" -> name = nonNullText()
                            "link" -> parseLink(this@allChildrenAutoIgnore, feed)
                            "subtitle" -> description = nullableText()
                        }
                    }
                }
            }

            konsumer.close()
            feed
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseLink(konsume: Konsumer, feed: Feed) = with(konsume) {
        val rel = attributes.getValueOrNull("rel")

        if (rel == "self")
            feed.url = attributes["href"]
        else if (rel == "alternate")
            feed.siteUrl = attributes["href"]
    }

    companion object {
        val names = Names.of("title", "link", "subtitle")
    }
}