package com.readrops.api.localfeed.rss1

import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.localfeed.XmlAdapter.Companion.AUTHORS_MAX
import com.readrops.api.utils.*
import com.readrops.db.entities.Item
import java.io.InputStream

class RSS1ItemsAdapter : XmlAdapter<List<Item>> {

    override fun fromXml(inputStream: InputStream): List<Item> {
        val konsume = inputStream.konsumeXml()
        val items = arrayListOf<Item>()

        return try {
            konsume.child("RDF") {
                allChildrenAutoIgnore("item") {
                    val authors = arrayListOf<String?>()
                    val about = attributes.getValueOpt("about",
                            namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")

                    val item = Item().apply {
                        allChildrenAutoIgnore(names) {
                            when (tagName) {
                                "title" -> title = nonNullText()
                                "link" -> link = nullableText()
                                "dc:date" -> pubDate = DateUtils.parse(nonNullText())
                                "dc:creator" -> authors += nullableText()
                                "description" -> description = nullableTextRecursively()
                                "content:encoded" -> content = nullableTextRecursively()
                                else -> skipContents()
                            }
                        }
                    }

                    if (item.link == null) item.link = about
                            ?: throw ParseException("RSS1 link or about element is required")
                    item.guid = item.link

                    if (authors.filterNotNull().isNotEmpty()) item.author = authors.filterNotNull()
                            .joinToString(limit = AUTHORS_MAX)

                    validateItem(item)

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
            item.pubDate == null -> throw ParseException("Item date is required")
        }
    }

    companion object {
        val names = Names.of("title", "description", "date", "link", "creator", "encoded")
    }
}