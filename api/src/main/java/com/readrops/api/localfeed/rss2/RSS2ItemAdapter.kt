package com.readrops.api.localfeed.rss2

import com.gitlab.mvysny.konsumexml.*
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.localfeed.XmlAdapter.Companion.AUTHORS_MAX
import com.readrops.api.utils.*
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nonNullText
import com.readrops.api.utils.extensions.nullableText
import com.readrops.api.utils.extensions.nullableTextRecursively
import com.readrops.db.entities.Item
import org.joda.time.LocalDateTime

class RSS2ItemAdapter : XmlAdapter<Item> {

    override fun fromXml(konsumer: Konsumer): Item {
        val item = Item()

        return try {
            //konsumer.checkCurrent("item")
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
            item
        } catch (e: KonsumerException) {
            throw ParseException(e.message)
        }
    }

    private fun parseEnclosure(konsumer: Konsumer, item: Item) = with(konsumer) {
        if (attributes.getValueOrNull("type") != null
                && ApiUtils.isMimeImage(attributes["type"]) && item.imageLink == null)
            item.imageLink = attributes.getValueOrNull("url")
    }

    private fun isMediumImage(konsumer: Konsumer) = with(konsumer) {
        attributes.getValueOrNull("medium") != null && ApiUtils.isMimeImage(attributes["medium"])
    }

    private fun isTypeImage(konsumer: Konsumer) = with(konsumer) {
        attributes.getValueOrNull("type") != null && ApiUtils.isMimeImage(attributes["type"])
    }

    private fun parseMediaContent(konsumer: Konsumer, item: Item) = with(konsumer) {
        if ((isMediumImage(konsumer) || isTypeImage(konsumer)) && item.imageLink == null)
            item.imageLink = konsumer.attributes.getValueOrNull("url")

        konsumer.skipContents() // ignore media content sub elements
    }

    private fun parseMediaGroup(konsumer: Konsumer, item: Item) = with(konsumer) {
        allChildrenAutoIgnore("content") {
            when (tagName) {
                "media:content" -> parseMediaContent(this, item)
                else -> skipContents()
            }
        }
    }

    private fun finalizeItem(item: Item, creators: List<String?>) = with(item) {
        validateItem(this)

        if (pubDate == null) pubDate = LocalDateTime.now()
        if (guid == null) guid = link
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