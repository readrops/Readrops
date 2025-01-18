package com.readrops.api.localfeed.atom

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.readrops.api.localfeed.RSSMedia
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nonNullText
import com.readrops.api.utils.extensions.nullableText
import com.readrops.api.utils.extensions.nullableTextRecursively
import com.readrops.db.entities.Item
import com.readrops.db.util.DateUtils
import java.time.LocalDateTime

class ATOMItemAdapter : XmlAdapter<Item> {

    override fun fromXml(konsumer: Konsumer): Item {
        val item = Item()

        return try {
            item.apply {
                konsumer.allChildrenAutoIgnore(names) {
                    when (tagName) {
                        "title" -> title = nonNullText()
                        "id" -> remoteId = nullableText()
                        "published" -> pubDate = DateUtils.parse(nullableText())
                        "updated" -> {
                            val updated = nullableText()
                            if (pubDate == null) {
                                pubDate = DateUtils.parse(updated)
                            }
                        }
                        "link" -> parseLink(this, this@apply)
                        "author" -> allChildrenAutoIgnore("name") { author = nullableText() }
                        "summary" -> description = nullableTextRecursively()
                        "content" -> content = nullableTextRecursively()
                        "media:group" -> RSSMedia.parseMediaGroup(this, item)
                        else -> skipContents()
                    }
                }
            }

            validateItem(item)
            if (item.pubDate == null) item.pubDate = LocalDateTime.now()
            if (item.remoteId == null) item.remoteId = item.link

            item
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseLink(konsumer: Konsumer, item: Item) = with(konsumer) {
        if (attributes.getValueOrNull("rel") == null ||
                attributes["rel"] == "alternate")
            item.link = attributes.getValueOrNull("href")
    }

    private fun validateItem(item: Item) {
        when {
            item.title == null -> throw ParseException("Item title is required")
            item.link == null -> throw ParseException("Item link is required")
        }
    }

    companion object {
        val names = Names.of("title", "id", "updated", "link", "author", "summary",
            "content", "group", "published")
    }
}