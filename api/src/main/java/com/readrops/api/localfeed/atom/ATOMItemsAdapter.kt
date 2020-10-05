package com.readrops.api.localfeed.atom

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.*
import com.readrops.db.entities.Item
import org.joda.time.LocalDateTime
import java.io.InputStream

class ATOMItemsAdapter : XmlAdapter<List<Item>> {

    override fun fromXml(inputStream: InputStream): List<Item> {
        val konsumer = inputStream.konsumeXml()
        val items = arrayListOf<Item>()

        return try {
            konsumer.child("feed") {
                allChildrenAutoIgnore("entry") {
                    val item = Item().apply {
                        allChildrenAutoIgnore(names) {
                            when (tagName) {
                                "title" -> title = nonNullText()
                                "id" -> guid = nullableText()
                                "updated" -> pubDate = DateUtils.parse(nullableText())
                                "link" -> parseLink(this, this@apply)
                                "author" -> allChildrenAutoIgnore("name") { author = nullableText() }
                                "summary" -> description = nullableTextRecursively()
                                "content" -> content = nullableTextRecursively()
                                else -> skipContents()
                            }
                        }
                    }

                    validateItem(item)
                    if (item.pubDate == null) item.pubDate = LocalDateTime.now()
                    if (item.guid == null) item.guid = item.link

                    items += item
                }
            }

            konsumer.close()
            items
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseLink(konsumer: Konsumer, item: Item) {
        konsumer.apply {
            if (attributes.getValueOpt("rel") == null ||
                    attributes["rel"] == "alternate")
                item.link = attributes.getValueOpt("href")
        }

    }

    private fun validateItem(item: Item) {
        when {
            item.title == null -> throw ParseException("Item title is required")
            item.link == null -> throw ParseException("Item link is required")
        }
    }

    companion object {
        val names = Names.of("title", "id", "updated", "link", "author", "summary", "content")
    }
}