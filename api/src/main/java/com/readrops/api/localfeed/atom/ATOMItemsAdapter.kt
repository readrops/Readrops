package com.readrops.api.localfeed.atom

import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.ParseException
import com.readrops.api.utils.nonNullText
import com.readrops.api.utils.nullableText
import com.readrops.db.entities.Item
import java.io.InputStream

class ATOMItemsAdapter : XmlAdapter<List<Item>> {

    override fun fromXml(inputStream: InputStream): List<Item> {
        val konsume = inputStream.konsumeXml()
        val items = arrayListOf<Item>()

        return try {
            konsume.child("feed") {
                allChildrenAutoIgnore("entry") {
                    val item = Item().apply {
                        allChildrenAutoIgnore(names) {
                            when (tagName) {
                                "title" -> title = nonNullText()
                                "id" -> guid = nullableText()
                                "updated" -> pubDate = DateUtils.stringToLocalDateTime(nonNullText())
                                "link" -> if (attributes["rel"] == "alternate") link = attributes["href"]
                                "author" -> allChildrenAutoIgnore("name") { author = text() }
                                "summary" -> description = nullableText()
                                "content" -> content = nullableText()
                            }
                        }
                    }

                    validateItem(item)
                    if (item.guid == null) item.guid = item.link

                    items += item
                }
            }

            konsume.close()
            items
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun validateItem(item: Item) {
        when {
            item.title == null -> throw ParseException("Item title is required")
            item.link == null -> throw ParseException("Item link is required")
            item.pubDate == null -> throw ParseException("Item date id required")
        }
    }

    companion object {
        val names = Names.of("title", "id", "updated", "link", "author", "summary", "content")
    }
}