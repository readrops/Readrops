package com.readrops.api.localfeed.rss2

import com.gitlab.mvysny.konsumexml.*
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.localfeed.XmlAdapter.Companion.AUTHORS_MAX
import com.readrops.api.utils.*
import com.readrops.db.entities.Item
import java.io.InputStream

class RSS2ItemsAdapter : XmlAdapter<List<Item>> {

    override fun fromXml(inputStream: InputStream): List<Item> {
        val konsume = inputStream.konsumeXml()
        val items = mutableListOf<Item>()

        return try {
            konsume.child("rss") {
                child("channel") {
                    allChildrenAutoIgnore("item") {
                        val enclosures = arrayListOf<String>()
                        val mediaContents = arrayListOf<String>()
                        val creators = arrayListOf<String?>()

                        val item = Item().apply {
                            allChildrenAutoIgnore(names) {
                                when (tagName) {
                                    "title" -> title = LibUtils.cleanText(nonNullText())
                                    "link" -> link = nonNullText()
                                    "author" -> author = nullableText()
                                    "dc:creator" -> creators += nullableText()
                                    "pubDate" -> pubDate = DateUtils.stringToLocalDateTime(nonNullText())
                                    "dc:date" -> pubDate = DateUtils.stringToLocalDateTime(nonNullText())
                                    "guid" -> guid = nullableText()
                                    "description" -> description = nullableTextRecursively()
                                    "content:encoded" -> content = nullableTextRecursively()
                                    "enclosure" -> parseEnclosure(this, enclosures)
                                    "media:content" -> parseMediaContent(this, mediaContents)
                                    "media:group" -> parseMediaGroup(this, mediaContents)
                                    else -> skipContents() // for example media:description
                                }
                            }
                        }

                        validateItem(item)
                        if (item.guid == null) item.guid = item.link
                        if (item.author == null && creators.filterNotNull().isNotEmpty())
                            item.author = creators.filterNotNull().joinToString(limit = AUTHORS_MAX)

                        if (enclosures.isNotEmpty()) item.imageLink = enclosures.first()
                        else if (mediaContents.isNotEmpty()) item.imageLink = mediaContents.first()

                        items += item
                    }
                }
            }

            konsume.close()
            items
        } catch (e: KonsumerException) {
            throw ParseException(e.message)
        }
    }

    private fun parseEnclosure(konsume: Konsumer, enclosures: MutableList<String>) {
        if (konsume.attributes.getValueOpt("type") != null
                && LibUtils.isMimeImage(konsume.attributes["type"]))
            enclosures += konsume.attributes["url"]
    }

    private fun parseMediaContent(konsume: Konsumer, mediaContents: MutableList<String>) {
        if (konsume.attributes.getValueOpt("medium") != null
                && LibUtils.isMimeImage(konsume.attributes["medium"]))
            mediaContents += konsume.attributes["url"]

        konsume.skipContents() // ignore media content sub elements
    }

    private fun parseMediaGroup(konsume: Konsumer, mediaContents: MutableList<String>) {
        konsume.allChildrenAutoIgnore("content") {
            when (tagName) {
                "media:content" -> parseMediaContent(this, mediaContents)
                else -> skipContents()
            }
        }
    }

    private fun validateItem(item: Item) {
        when {
            item.title == null -> throw ParseException("Item title is required")
            item.link == null -> throw ParseException("Item link is required")
            item.pubDate == null -> throw ParseException("Item date is required")
        }
    }

    companion object {
        val names = Names.of("title", "link", "author", "creator", "pubDate", "date",
                "guid", "description", "encoded", "enclosure", "content", "group")
    }
}