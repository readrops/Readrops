package com.readrops.api.services.fever.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.api.utils.extensions.skipField
import com.readrops.api.utils.extensions.toBoolean
import com.readrops.db.entities.Item
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import org.joda.time.LocalDateTime

class FeverItemsAdapter {

    @ToJson
    fun toJson(items: List<Item>) = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<Item> = with(reader) {
        return try {
            val items = arrayListOf<Item>()

            beginObject()

            repeat(4) {
                skipField()
            }

            nextName() // beginning of items array
            beginArray()

            while (hasNext()) {
                beginObject()

                val item = Item()
                while (hasNext()) {
                    with(item) {
                        when (selectName(NAMES)) {
                            0 -> remoteId = nextNonEmptyString()
                            1 -> feedRemoteId = nextInt().toString()
                            2 -> title = nextNonEmptyString()
                            3 -> author = nextNullableString()
                            4 -> content = nextNullableString()
                            5 -> link = nextNullableString()
                            6 -> isRead = nextInt().toBoolean()
                            7 -> pubDate = LocalDateTime(nextLong() * 1000L)
                            else -> skipValue()
                        }
                    }
                }

                items += item
                endObject()
            }

            endArray()
            endObject()

            items
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of(
            "id", "feed_id", "title", "author", "html", "url",
            "is_read", "created_on_time"
        )
    }
}