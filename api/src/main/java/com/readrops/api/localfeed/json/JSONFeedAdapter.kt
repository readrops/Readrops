package com.readrops.api.localfeed.json

import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.squareup.moshi.*

class JSONFeedAdapter : JsonAdapter<Pair<Feed, List<Item>>>() {

    override fun toJson(writer: JsonWriter, value: Pair<Feed, List<Item>>?) {
        TODO("Not yet implemented")
    }

    override fun fromJson(reader: JsonReader): Pair<Feed, List<Item>> = try {
        val feed = Feed()
        val items = arrayListOf<Item>()

        val itemAdapter = JSONItemsAdapter()

        reader.beginObject()

        while (reader.hasNext()) {
            with(feed) {
                when (reader.selectName(names)) {
                    0 -> name = reader.nextNonEmptyString()
                    1 -> siteUrl = reader.nextNullableString()
                    2 -> url = reader.nextNullableString()
                    3 -> description = reader.nextNullableString()
                    4 -> items += itemAdapter.fromJson(reader)
                    else -> reader.skipValue()
                }
            }
        }

        reader.endObject()
        Pair(feed, items)
    } catch (e: Exception) {
        throw ParseException(e.message)
    }

    companion object {
        val names: JsonReader.Options = JsonReader.Options.of("title", "home_page_url",
                "feed_url", "description", "items")
    }
}