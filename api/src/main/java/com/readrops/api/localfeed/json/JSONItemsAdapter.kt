package com.readrops.api.localfeed.json

import com.readrops.api.localfeed.XmlAdapter.Companion.AUTHORS_MAX
import com.readrops.api.utils.DateUtils
import com.readrops.api.utils.ParseException
import com.readrops.api.utils.nextNullableString
import com.readrops.db.entities.Item
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class JSONItemsAdapter : JsonAdapter<List<Item>>() {

    override fun toJson(writer: JsonWriter, value: List<Item>?) {
        // not useful
    }

    @FromJson
    override fun fromJson(reader: JsonReader): List<Item> {
        try {
            val items = arrayListOf<Item>()
            reader.beginObject()

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "items" -> parseItems(reader, items)
                    else -> reader.skipValue()
                }
            }

            return items
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseItems(reader: JsonReader, items: MutableList<Item>) {
        reader.beginArray()

        while (reader.hasNext()) {
            reader.beginObject()
            val item = Item()

            var contentText: String? = null
            var contentHtml: String? = null

            while (reader.hasNext()) {
                with(item) {
                    when (reader.selectName(names)) {
                        0 -> guid = reader.nextString()
                        1 -> link = reader.nextString()
                        2 -> title = reader.nextString()
                        3 -> contentHtml = reader.nextNullableString()
                        4 -> contentText = reader.nextNullableString()
                        5 -> description = reader.nextNullableString()
                        6 -> imageLink = reader.nextNullableString()
                        7 -> pubDate = DateUtils.stringToLocalDateTime(reader.nextString())
                        8 -> author = parseAuthor(reader) // jsonfeed 1.0
                        9 -> author = parseAuthors(reader) // jsonfeed 1.1
                        else -> reader.skipValue()
                    }
                }
            }

            validateItem(item)
            item.content = if (contentHtml != null) contentHtml else contentText

            reader.endObject()
            items += item
        }

        reader.endArray()
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
            authors.add(parseAuthor(reader))
        }

        reader.endArray()

        // here, nextNullableString doesn't check if authors values are empty
        return if (authors.filterNot { author -> author.isNullOrEmpty() }.isNotEmpty())
            authors.filterNot { author -> author.isNullOrEmpty() }.joinToString(limit = AUTHORS_MAX) else null
    }

    private fun validateItem(item: Item) {
        when {
            item.title == null -> throw ParseException("Item title is required")
            item.link == null -> throw ParseException("Item link is required")
            item.pubDate == null -> throw ParseException("Item date id required")
        }
    }

    companion object {
        val names: JsonReader.Options = JsonReader.Options.of("id", "url", "title",
                "content_html", "content_text", "summary", "image", "date_published", "author", "authors")
    }
}