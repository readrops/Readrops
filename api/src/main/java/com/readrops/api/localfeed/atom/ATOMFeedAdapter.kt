package com.readrops.api.localfeed.atom

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nonNullText
import com.readrops.api.utils.extensions.nullableText
import com.readrops.db.entities.Feed
import java.io.InputStream

class ATOMFeedAdapter : XmlAdapter<Feed> {

    override fun fromXml(inputStream: InputStream): Feed {
        val konsume = inputStream.konsumeXml()
        val feed = Feed()

        return try {
            konsume.child("feed") {
                allChildrenAutoIgnore(names) {
                    with(feed) {
                        when (tagName) {
                            "title" -> name = nonNullText()
                            "link" -> parseLink(this@allChildrenAutoIgnore, feed)
                            "subtitle" -> description = nullableText()
                        }
                    }
                }
            }

            konsume.close()
            feed
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseLink(konsume: Konsumer, feed: Feed) {
        val rel = konsume.attributes.getValueOpt("rel")

        if (rel == "self")
            feed.url = konsume.attributes["href"]
        else if (rel == "alternate")
            feed.siteUrl = konsume.attributes["href"]
    }

    companion object {
        val names = Names.of("title", "link", "subtitle")
    }
}