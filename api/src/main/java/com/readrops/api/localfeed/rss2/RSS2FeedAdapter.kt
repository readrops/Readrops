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
import org.jsoup.Jsoup

class RSS2FeedAdapter : XmlAdapter<Feed> {

    override fun fromXml(konsumer: Konsumer): Feed {
        val feed = Feed()

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
                                    if (attributes.getValueOpt("rel") == "self")
                                        url = attributes.getValueOpt("href")
                                }
                                else -> skipContents()
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
        val names = Names.of("title", "description", "link")
    }
}