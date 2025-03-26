package com.readrops.api.localfeed.rss1

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.localfeed.XmlAdapter.Companion.AUTHORS_MAX
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nonNullText
import com.readrops.api.utils.extensions.nullableText
import com.readrops.api.utils.extensions.nullableTextRecursively
import com.readrops.db.entities.Item
import com.readrops.db.util.DateUtils
import java.time.LocalDateTime

class RSS1ItemAdapter : XmlAdapter<Item> {

    override fun fromXml(konsumer: Konsumer): Item {
        val item = Item()

        val authors = arrayListOf<String?>()
        val about = konsumer.attributes.getValueOrNull(
            "about",
            namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        )

        return item.apply {
            konsumer.allChildrenAutoIgnore(names) {
                when (tagName) {
                    "title" -> title = nonNullText()
                    "link" -> link = nullableText()
                    "dc:date" -> pubDate = DateUtils.parse(nullableText())
                    "dc:creator" -> authors += nullableText()
                    "description" -> description = nullableTextRecursively()
                    "content:encoded" -> content = nullableTextRecursively()
                    else -> skipContents()
                }
            }

            validateItem(this)

            if (pubDate == null) pubDate = LocalDateTime.now()
            if (link == null) link = about
                ?: throw ParseException("RSS1 link or about element is required")
            remoteId = link

            if (authors.filterNotNull().isNotEmpty()) author = authors.filterNotNull()
                .joinToString(limit = AUTHORS_MAX)
        }
    }

    private fun validateItem(item: Item) {
        if (item.title == null) throw ParseException("Item title is required")
    }

    companion object {
        val names = Names.of("title", "description", "date", "link", "creator", "encoded")
    }
}