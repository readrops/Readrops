package com.readrops.api.services.greader.adapters

import com.readrops.api.services.greader.GReaderDataSource.Companion.GOOGLE_READ
import com.readrops.api.services.greader.GReaderDataSource.Companion.GOOGLE_STARRED
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.db.entities.Item
import com.readrops.db.util.DateUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class GReaderItemsAdapter : JsonAdapter<List<Item>>() {

    override fun toJson(writer: JsonWriter, value: List<Item>?) {
        // no need of this
    }

    override fun fromJson(reader: JsonReader): List<Item> = with(reader) {
        val items = mutableListOf<Item>()

        return try {
            beginObject()

            while (hasNext()) {
                when (nextName()) {
                    "items" -> parseItems(reader, items)
                    else -> skipValue()
                }
            }

            endObject()
            items
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseItems(reader: JsonReader, items: MutableList<Item>) = with(reader) {
        beginArray()

        while (hasNext()) {
            val item = Item()
            beginObject()

            while (hasNext()) {
                with(item) {
                    when (selectName(NAMES)) {
                        0 -> remoteId = nextNonEmptyString()
                        1 -> pubDate = DateUtils.fromEpochSeconds(nextLong())
                        2 -> title = nextNonEmptyString()
                        3 -> content = getContent(reader)
                        4 -> link = getLink(reader)
                        5 -> getStates(reader, this)
                        6 -> feedRemoteId = getRemoteFeedId(reader)
                        7 -> author = nextNullableString()
                        else -> skipValue()
                    }
                }
            }

            items += item
            endObject()
        }

        endArray()
    }

    private fun getContent(reader: JsonReader): String? = with(reader) {
        var content: String? = null
        beginObject()

        while (hasNext()) {
            when (nextName()) {
                "content" -> content = nextNullableString()
                else -> skipValue()
            }
        }

        endObject()
        return content
    }

    private fun getLink(reader: JsonReader): String? = with(reader) {
        var href: String? = null
        beginArray()

        while (hasNext()) {
            beginObject()

            while (hasNext()) {
                when (nextName()) {
                    "href" -> href = nextString()
                    else -> skipValue()
                }
            }

            endObject()
        }

        endArray()
        return href
    }

    private fun getStates(reader: JsonReader, item: Item) = with(reader) {
        beginArray()

        while (hasNext()) {
            when (nextString()) {
                GOOGLE_READ -> item.isRead = true
                GOOGLE_STARRED -> item.isStarred = true
            }
        }

        endArray()
    }

    private fun getRemoteFeedId(reader: JsonReader): String? = with(reader) {
        var remoteFeedId: String? = null
        beginObject()

        while (hasNext()) {
            when (nextName()) {
                "streamId" -> remoteFeedId = nextString()
                else -> skipValue()
            }
        }

        endObject()
        return remoteFeedId
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of(
            "id", "published", "title", "summary", "alternate", "categories", "origin", "author"
        )
    }
}