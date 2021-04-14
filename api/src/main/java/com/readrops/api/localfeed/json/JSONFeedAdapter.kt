package com.readrops.api.localfeed.json

import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.db.entities.Feed
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

class JSONFeedAdapter {

    @ToJson
    fun toJson(feed: Feed) = ""

    @FromJson
    fun fromJson(reader: JsonReader): Feed = try {
        val feed = Feed()
        reader.beginObject()

        while (reader.hasNext()) {
            with(feed) {
                when (reader.selectName(names)) {
                    0 -> name = reader.nextNonEmptyString()
                    1 -> siteUrl = reader.nextNullableString()
                    2 -> url = reader.nextNullableString()
                    3 -> description = reader.nextNullableString()
                    else -> reader.skipValue()
                }
            }
        }

        reader.endObject()
        feed
    } catch (e: Exception) {
        throw ParseException(e.message)
    }

    companion object {
        val names: JsonReader.Options = JsonReader.Options.of("title", "home_page_url",
                "feed_url", "description")
    }
}