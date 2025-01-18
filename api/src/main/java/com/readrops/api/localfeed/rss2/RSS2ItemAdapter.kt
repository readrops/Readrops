package com.readrops.api.localfeed.rss2

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.KonsumerException
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.readrops.api.localfeed.RSSMedia
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.localfeed.XmlAdapter.Companion.AUTHORS_MAX
import com.readrops.api.utils.ApiUtils
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nonNullText
import com.readrops.api.utils.extensions.nullableText
import com.readrops.api.utils.extensions.nullableTextRecursively
import com.readrops.db.entities.Item
import com.readrops.db.util.DateUtils
import java.time.LocalDateTime

class RSS2ItemAdapter : XmlAdapter<Item> {

    override fun fromXml(konsumer: Konsumer): Item {
        val item = Item()

        return try {
            val creators = arrayListOf<String?>()

            item.apply {
                konsumer.allChildrenAutoIgnore(names) {
                    when (tagName) {
                        "title" -> title = ApiUtils.cleanText(nonNullText())
                        "link" -> link = nonNullText()
                        "author" -> author = nullableText()
                        "dc:creator" -> creators += nullableText()
                        "pubDate" -> pubDate = DateUtils.parse(nullableText())
                        "dc:date" -> pubDate = DateUtils.parse(nullableText())
                        "guid" -> remoteId = nullableText()
                        "description" -> description = nullableTextRecursively()
                        "content:encoded" -> content = nullableTextRecursively()
                        "enclosure" -> RSSMedia.parseMediaContent(this, item = this@apply)
                        "media:content" -> RSSMedia.parseMediaContent(this, item = this@apply)
                        "media:group" -> RSSMedia.parseMediaGroup(this, item = this@apply)
                        else -> skipContents() // for example media:description
                    }
                }
            }

            finalizeItem(item, creators)
            item
        } catch (e: KonsumerException) {
            throw ParseException(e.message)
        }
    }

    private fun finalizeItem(item: Item, creators: List<String?>) = with(item) {
        validateItem(this)

        if (pubDate == null) pubDate = LocalDateTime.now()
        if (remoteId == null) remoteId = link
        if (author == null && creators.filterNotNull().isNotEmpty())
            author = creators.filterNotNull().joinToString(limit = AUTHORS_MAX)
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