package com.readrops.api.localfeed.json

import com.readrops.api.utils.nextNullableString
import com.readrops.db.entities.Feed
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

class JSONFeedAdapter {

    @ToJson
    fun toJson(feed: Feed) = ""

    @FromJson
    fun fromJson(reader: JsonReader): Feed {
        val feed = Feed()
        reader.beginObject()

        while (reader.hasNext()) {
            with(feed) {
                when (reader.selectName(names)) {
                    0 -> name = reader.nextString()
                    1 -> siteUrl = reader.nextNullableString()
                    2 -> url = reader.nextNullableString()
                    3 -> description = reader.nextNullableString()
                    else -> reader.skipValue()
                }
            }
        }

        reader.endObject()
        return feed
    }

    companion object {
        val names: JsonReader.Options = JsonReader.Options.of("title", "home_page_url",
                "feed_url", "description")
    }
}