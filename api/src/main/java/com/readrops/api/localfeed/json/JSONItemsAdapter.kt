package com.readrops.api.localfeed.json

import com.readrops.api.localfeed.XmlAdapter.Companion.AUTHORS_MAX
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.db.entities.Item
import com.readrops.db.util.DateUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.time.LocalDateTime

class JSONItemsAdapter : JsonAdapter<List<Item>>() {

    override fun toJson(writer: JsonWriter, value: List<Item>?) {
        // not useful
    }

    override fun fromJson(reader: JsonReader): List<Item> = with(reader) {
        val items = arrayListOf<Item>()

        try {
            beginArray()

            while (hasNext()) {
                beginObject()
                val item = Item()

                var contentText: String? = null
                var contentHtml: String? = null

                while (hasNext()) {
                    with(item) {
                        when (selectName(names)) {
                            0 -> remoteId = nextNonEmptyString()
                            1 -> link = nextNonEmptyString()
                            2 -> title = nextNonEmptyString()
                            3 -> contentHtml = nextNullableString()
                            4 -> contentText = nextNullableString()
                            5 -> description = nextNullableString()
                            6 -> imageLink = nextNullableString()
                            7 -> pubDate = DateUtils.parse(nextNullableString())
                            8 -> author = parseAuthor(reader) // jsonfeed 1.0
                            9 -> author = parseAuthors(reader) // jsonfeed 1.1
                            else -> skipValue()
                        }
                    }
                }

                validateItem(item)
                item.content = if (contentHtml != null) contentHtml else contentText
                if (item.pubDate == null) item.pubDate = LocalDateTime.now()

                endObject()
                items += item
            }

            endArray()
            items
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseAuthor(reader: JsonReader): String? {
        var author: String? = null
        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "name" -> author = reader.nextNullableString()
                else -> reader.skipValue()
            }
        }

        reader.endObject()
        return author
    }

    private fun parseAuthors(reader: JsonReader): String? {
        val authors = arrayListOf<String?>()
        reader.beginArray()

        while (reader.hasNext()) {
            authors += parseAuthor(reader)
        }

        reader.endArray()

        return if (authors.filterNotNull().isNotEmpty())
            authors.filterNotNull().joinToString(limit = AUTHORS_MAX) else null
    }

    private fun validateItem(item: Item): Boolean = when {
        item.title == null -> throw ParseException("Item title is required")
        item.link == null -> throw ParseException("Item link is required")
        else -> true
    }

    companion object {
        val names: JsonReader.Options = JsonReader.Options.of("id", "url", "title",
                "content_html", "content_text", "summary", "image", "date_published", "author", "authors")
    }
}