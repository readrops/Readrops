package com.readrops.api.services.fever.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.api.utils.extensions.toBoolean
import com.readrops.db.entities.Item
import com.readrops.db.util.DateUtils
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

class FeverItemsAdapter {

    @ToJson
    fun toJson(items: List<Item>) = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<Item> = with(reader) {
        return try {
            val items = arrayListOf<Item>()

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "items" -> {
                        beginArray()
                        while (hasNext()) {
                            beginObject()
                            items += parseItem(reader)

                            endObject()
                        }

                        endArray()
                    }
                    else -> skipValue()
                }
            }

            endObject()
            items
        } catch (e: Exception) {
            throw ParseException("Fever items parsing failure", e)
        }
    }

    private fun parseItem(reader: JsonReader): Item = with(reader) {
        val item = Item()

        while (hasNext()) {
            with(item) {
                when (selectName(NAMES)) {
                    0 -> {
                        remoteId = if (reader.peek() == JsonReader.Token.STRING) {
                            nextNonEmptyString()
                        } else {
                            nextInt().toString()
                        }
                    }
                    1 -> feedRemoteId = nextNonEmptyString()
                    2 -> title = nextNonEmptyString()
                    3 -> author = nextNullableString()
                    4 -> content = nextNullableString()
                    5 -> link = nextNullableString()
                    6 -> isRead = nextInt().toBoolean()
                    7 -> isStarred = nextInt().toBoolean()
                    8 -> pubDate = DateUtils.fromEpochSeconds(nextLong())
                    else -> skipValue()
                }
            }
        }

        return item
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of(
            "id", "feed_id", "title", "author", "html", "url",
            "is_read", "is_saved", "created_on_time"
        )
    }
}