package com.readrops.api.services.freshrss.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.db.entities.Feed
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

class FreshRSSFeedsAdapter {

    @ToJson
    fun toJson(feed: Feed): String = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<Feed> {
        val feeds = mutableListOf<Feed>()

        return try {
            reader.beginObject()
            reader.nextName() // "subscriptions", beginning of the feed array
            reader.beginArray()

            while (reader.hasNext()) {
                reader.beginObject()

                val feed = Feed()
                while (reader.hasNext()) {
                    with(feed) {
                        when (reader.selectName(NAMES)) {
                            0 -> name = reader.nextNonEmptyString()
                            1 -> url = reader.nextNonEmptyString()
                            2 -> siteUrl = reader.nextNullableString()
                            3 -> iconUrl = reader.nextNullableString()
                            4 -> remoteId = reader.nextNonEmptyString()
                            5 -> remoteFolderId = getCategoryId(reader)
                            else -> reader.skipValue()
                        }
                    }
                }

                feeds += feed
                reader.endObject()
            }

            reader.endArray()
            reader.endObject()

            feeds
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun getCategoryId(reader: JsonReader): String? {
        var id: String? = null
        reader.beginArray()

        while (reader.hasNext()) {
            reader.beginObject()

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> id = reader.nextNullableString()
                    else -> reader.skipValue()
                }
            }

            reader.endObject()
            if (!id.isNullOrEmpty())
                break
        }

        reader.endArray()
        return id
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of("title", "url", "htmlUrl",
                "iconUrl", "id", "categories")
    }
}