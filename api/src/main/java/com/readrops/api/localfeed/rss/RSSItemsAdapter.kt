package com.readrops.api.localfeed.rss

import com.gitlab.mvysny.konsumexml.*
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.*
import com.readrops.db.entities.Item
import java.io.InputStream

class RSSItemsAdapter : XmlAdapter<List<Item>> {

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
                                    "description" -> description = text(failOnElement = false)
                                    "content:encoded" -> content = nullableText(failOnElement = false)
                                    "enclosure" -> parseEnclosure(this, enclosures)
                                    "media:content" -> parseMediaContent(this, mediaContents)
                                    "media:group" -> allChildrenAutoIgnore("content") {
                                        when (tagName) {
                                            "media:content" -> parseMediaContent(this, mediaContents)
                                        }
                                    }
                                    else -> skipContents() // for example media:description
                                }
                            }
                        }

                        validateItem(item)
                        if (item.guid == null) item.guid = item.link
                        if (item.author == null && creators.filterNotNull().isNotEmpty())
                            item.author = creators.filterNotNull().first()

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

    private fun validateItem(item: Item) {
        when {
            item.title == null -> throw ParseException("Item title can't be null")
            item.link == null -> throw ParseException("Item link can't be null")
            item.pubDate == null -> throw ParseException("Item date can't be null")
        }
    }

    companion object {
        val names = Names.of("title", "link", "author", "creator", "pubDate", "date",
                "guid", "description", "encoded", "enclosure", "content", "group")
    }
}