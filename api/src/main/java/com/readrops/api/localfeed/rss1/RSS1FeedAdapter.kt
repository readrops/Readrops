package com.readrops.api.localfeed.rss1

import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.ParseException
import com.readrops.api.utils.nonNullText
import com.readrops.api.utils.nullableText
import com.readrops.db.entities.Feed
import java.io.InputStream

class RSS1FeedAdapter : XmlAdapter<Feed> {

    override fun fromXml(inputStream: InputStream): Feed {
        val konsume = inputStream.konsumeXml()
        val feed = Feed()

        return try {
            konsume.child("RDF") {
                allChildrenAutoIgnore("channel") {
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

            konsume.close()
            feed
        } catch (e: Exception) {
            throw ParseException(e.message)
        }

    }

    companion object {
        val names = Names.of("title", "link", "description")
    }
}