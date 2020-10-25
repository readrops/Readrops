package com.readrops.api.services.freshrss.adapters

import android.util.TimingLogger
import com.readrops.db.entities.Item
import com.readrops.api.services.freshrss.FreshRSSDataSource.GOOGLE_READ
import com.readrops.api.utils.exceptions.ParseException
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

class FreshRSSItemsAdapter : JsonAdapter<List<Item>>() {

    override fun toJson(writer: JsonWriter, value: List<Item>?) {
        // no need of this
    }

    override fun fromJson(reader: JsonReader): List<Item>? {
        val logger = TimingLogger(TAG, "item parsing")
        val items = mutableListOf<Item>()

        return try {
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.nextName() == "items") parseItems(reader, items) else reader.skipValue()
            }

            reader.endObject()

            logger.addSplit("item parsing done")
            logger.dumpToLog()

            items
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseItems(reader: JsonReader, items: MutableList<Item>) {
        reader.beginArray()

        while (reader.hasNext()) {
            val item = Item()
            reader.beginObject()

            while (reader.hasNext()) {
                with(item) {
                    when (reader.selectName(NAMES)) {
                        0 -> remoteId = reader.nextString()
                        1 -> pubDate = LocalDateTime(reader.nextLong() * 1000L,
                                DateTimeZone.getDefault())
                        2 -> title = reader.nextString()
                        3 -> content = getContent(reader)
                        4 -> link = getLink(reader)
                        5 -> isRead = getReadState(reader)
                        6 -> feedRemoteId = getRemoteFeedId(reader)
                        7 -> author = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
            }

            items += item
            reader.endObject()
        }

        reader.endArray()
    }

    private fun getContent(reader: JsonReader): String? {
        var content: String? = null
        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "content" -> content = reader.nextString()
                else -> reader.skipValue()
            }
        }

        reader.endObject()
        return content
    }

    private fun getLink(reader: JsonReader): String? {
        var href: String? = null
        reader.beginArray()

        while (reader.hasNext()) {
            reader.beginObject()

            when (reader.nextName()) {
                "href" -> href = reader.nextString()
                else -> reader.skipValue()
            }

            reader.endObject()
        }

        reader.endArray()
        return href
    }

    private fun getReadState(reader: JsonReader): Boolean {
        var isRead = false
        reader.beginArray()

        while (reader.hasNext()) {
            when (reader.nextString()) {
                GOOGLE_READ -> isRead = true
            }
        }

        reader.endArray()
        return isRead
    }

    private fun getRemoteFeedId(reader: JsonReader): String? {
        var remoteFeedId: String? = null
        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "streamId" -> remoteFeedId = reader.nextString()
                else -> reader.skipValue()
            }
        }

        reader.endObject()
        return remoteFeedId
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of("id", "published", "title",
                "summary", "alternate", "categories", "origin", "author")

        val TAG = FreshRSSItemsAdapter::class.java.simpleName
    }
}