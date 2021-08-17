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

class RSS1FeedAdapter : XmlAdapter<Feed> {

    override fun fromXml(konsumer: Konsumer): Feed {
        val feed = Feed()

        return try {
            konsumer.checkElement(LocalRSSHelper.RSS_1_ROOT_NAME) {
                it.allChildrenAutoIgnore("channel") {
                    feed.url = attributes.getValueOpt("about",
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
            }

            konsumer.close()
            feed
        } catch (e: Exception) {
            throw ParseException(e.message)
        }

    }

    companion object {
        val names = Names.of("title", "link", "description")
    }
}