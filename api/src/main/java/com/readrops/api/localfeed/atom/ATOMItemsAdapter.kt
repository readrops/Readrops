package com.readrops.api.localfeed.atom

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.*
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nonNullText
import com.readrops.api.utils.extensions.nullableText
import com.readrops.api.utils.extensions.nullableTextRecursively
import com.readrops.db.entities.Item
import org.joda.time.LocalDateTime

class ATOMItemsAdapter : XmlAdapter<List<Item>> {

    override fun fromXml(konsumer: Konsumer): List<Item> {
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