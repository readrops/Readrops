package com.readrops.api.services.greader.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.db.entities.Feed
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

class GReaderFeedsAdapter {

    @ToJson
    fun toJson(feeds: List<Feed>): String = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<Feed> = with(reader) {
        val feeds = mutableListOf<Feed>()

        return try {
            beginObject()

            while (hasNext()) {
                when (nextName()) {
                    "subscriptions" -> {
                        beginArray()

                        while (hasNext()) {
                            beginObject()
                            feeds += parseFeed(reader)

                            endObject()
                        }

                        endArray()
                    }
                    else -> skipValue()
                }
            }

            endObject()
            feeds
        } catch (e: Exception) {
            throw ParseException("GReader feeds parsing failure", e)
        }
    }

    private fun parseFeed(reader: JsonReader): Feed = with(reader) {
        val feed = Feed()

        while (hasNext()) {
            with(feed) {
                when (selectName(NAMES)) {
                    0 -> name = nextNonEmptyString()
                    1 -> url = nextNonEmptyString()
                    2 -> siteUrl = nextNullableString()
                    3 -> iconUrl = nextNullableString()
                    4 -> remoteId = nextNonEmptyString()
                    5 -> remoteFolderId = getCategoryId(reader)
                    else -> skipValue()
                }
            }
        }

        return feed
    }

    private fun getCategoryId(reader: JsonReader): String? = with(reader) {
        var id: String? = null
        beginArray()

        while (hasNext()) {
            beginObject()

            while (hasNext()) {
                when (nextName()) {
                    "id" -> id = nextNullableString()
                    else -> skipValue()
                }
            }

            endObject()
            if (!id.isNullOrEmpty())
                break
        }

        endArray()
        return id
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of(
            "title", "url", "htmlUrl",
            "iconUrl", "id", "categories"
        )
    }
}