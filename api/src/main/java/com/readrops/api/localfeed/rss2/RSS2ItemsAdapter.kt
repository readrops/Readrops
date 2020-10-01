package com.readrops.api.localfeed.rss2

import com.gitlab.mvysny.konsumexml.*
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.localfeed.XmlAdapter.Companion.AUTHORS_MAX
import com.readrops.api.utils.*
import com.readrops.db.entities.Item
import org.joda.time.LocalDateTime
import java.io.InputStream

class RSS2ItemsAdapter : XmlAdapter<List<Item>> {

    override fun fromXml(inputStream: InputStream): List<Item> {
        val konsumer = inputStream.konsumeXml()
        val items = mutableListOf<Item>()

        return try {
            konsumer.child("rss") {
                child("channel") {
                    allChildrenAutoIgnore("item") {
                        val creators = arrayListOf<String?>()

                        val item = Item().apply {
                            allChildrenAutoIgnore(names) {
                                when (tagName) {
                                    "title" -> title = LibUtils.cleanText(nonNullText())
                                    "link" -> link = nonNullText()
                                    "author" -> author = nullableText()
                                    "dc:creator" -> creators += nullableText()
                                    "pubDate" -> pubDate = DateUtils.parse(nullableText())
                                    "dc:date" -> pubDate = DateUtils.parse(nullableText())
                                    "guid" -> guid = nullableText()
                                    "description" -> description = nullableTextRecursively()
                                    "content:encoded" -> content = nullableTextRecursively()
                                    "enclosure" -> parseEnclosure(this, item = this@apply)
                                    "media:content" -> parseMediaContent(this, item = this@apply)
                                    "media:group" -> parseMediaGroup(this, item = this@apply)
                                    else -> skipContents() // for example media:description
                                }
                            }
                        }

                        finalizeItem(item, creators)

                        items += item
                    }
                }
            }

            konsumer.close()
            items
        } catch (e: KonsumerException) {
            throw ParseException(e.message)
        }
    }

    private fun parseEnclosure(konsumer: Konsumer, item: Item) {
        if (konsumer.attributes.getValueOpt("type") != null
                && LibUtils.isMimeImage(konsumer.attributes["type"]) && item.imageLink == null)
            item.imageLink = konsumer.attributes.getValueOpt("url")
    }

    private fun parseMediaContent(konsumer: Konsumer, item: Item) {
        if (konsumer.attributes.getValueOpt("medium") != null
                && LibUtils.isMimeImage(konsumer.attributes["medium"]) && item.imageLink == null)
            item.imageLink = konsumer.attributes.getValueOpt("url")

        konsumer.skipContents() // ignore media content sub elements
    }

    private fun parseMediaGroup(konsumer: Konsumer, item: Item) {
        konsumer.allChildrenAutoIgnore("content") {
            when (tagName) {
                "media:content" -> parseMediaContent(this, item)
                else -> skipContents()
            }
        }
    }

    private fun finalizeItem(item: Item, creators: List<String?>) {
        item.apply {
            validateItem(this)

            if (pubDate == null) pubDate = LocalDateTime.now()
            if (guid == null) guid = link
            if (author == null && creators.filterNotNull().isNotEmpty())
                author = creators.filterNotNull().joinToString(limit = AUTHORS_MAX)
        }
    }

    private fun validateItem(item: Item) {
        when {
            item.title == null -> throw ParseException("Item title is required")
            item.link == null -> throw ParseException("Item link is required")
        }
    }

    companion object {
        val names = Names.of("title", "link", "author", "creator", "pubDate", "date",
                "guid", "description", "encoded", "enclosure", "content", "group")
    }
}