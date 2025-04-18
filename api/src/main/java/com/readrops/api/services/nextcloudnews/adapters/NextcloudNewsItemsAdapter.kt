package com.readrops.api.services.nextcloudnews.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.ApiUtils
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNullableLong
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.db.entities.Item
import com.readrops.db.util.DateUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.time.LocalDateTime

class NextcloudNewsItemsAdapter : JsonAdapter<List<Item>>() {

    override fun toJson(writer: JsonWriter, value: List<Item>?) {
        // no need of this
    }

    @SuppressLint("CheckResult")
    @Override
    override fun fromJson(reader: JsonReader): List<Item> {
        val items = mutableListOf<Item>()

        return try {
            reader.beginObject()
            reader.nextName() // "items", beginning of items array
            reader.beginArray()

            while (reader.hasNext()) {
                val item = Item()
                reader.beginObject()

                var enclosureMime: String? = null
                var enclosureLink: String? = null

                while (reader.hasNext()) {
                    with(item) {
                        when (reader.selectName(NAMES)) {
                            0 -> remoteId = reader.nextInt().toString()
                            1 -> link = reader.nextNullableString()
                            2 -> title = reader.nextNullableString()
                            3 -> author = reader.nextNullableString()
                            4 -> {
                                val value = reader.nextNullableLong()

                                pubDate = if (value != null) {
                                    DateUtils.fromEpochSeconds(value)
                                } else {
                                    LocalDateTime.now()
                                }
                            }

                            5 -> content = reader.nextNullableString()
                            6 -> enclosureMime = reader.nextNullableString()
                            7 -> enclosureLink = reader.nextNullableString()
                            8 -> feedRemoteId = reader.nextInt().toString()
                            9 -> isRead = !reader.nextBoolean() // the negation is important here
                            10 -> isStarred = reader.nextBoolean()
                            else -> reader.skipValue()
                        }
                    }
                }

                if (enclosureMime != null && ApiUtils.isMimeImage(enclosureMime!!)) {
                    item.imageLink = enclosureLink
                }

                if (item.title != null) {
                    items += item
                }

                reader.endObject()
            }

            reader.endArray()
            reader.endObject()

            items
        } catch (e: Exception) {
            throw ParseException("Nextcloud News items parsing failure", e)
        }
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of(
            "id", "url", "title", "author",
            "pubDate", "body", "enclosureMime", "enclosureLink", "feedId", "unread", "starred"
        )
    }
}