package com.readrops.api.services.nextcloudnews.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableInt
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.db.entities.Feed
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.net.URI

class NextNewsFeedsAdapter {

    @ToJson
    fun toJson(feeds: List<Feed>): String = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<Feed> {
        val feeds = mutableListOf<Feed>()

        return try {
            reader.beginObject()

            while (reader.hasNext()) {
                if (reader.nextName() == "feeds") parseFeeds(reader, feeds) else reader.skipValue()
            }

            reader.endObject()

            feeds
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    private fun parseFeeds(reader: JsonReader, feeds: MutableList<Feed>) {
        reader.beginArray()

        while (reader.hasNext()) {
            val feed = Feed()
            reader.beginObject()

            while (reader.hasNext()) {
                with(feed) {
                    when (reader.selectName(NAMES)) {
                        0 -> remoteId = reader.nextNonEmptyString()
                        1 -> url = reader.nextNonEmptyString()
                        2 -> name = reader.nextNullableString()
                        3 -> iconUrl = reader.nextNullableString()
                        4 -> {
                            val nextInt = reader.nextNullableInt()
                            remoteFolderId = if (nextInt != null && nextInt > 0) nextInt.toString() else null
                        }
                        5 -> siteUrl = reader.nextNullableString()
                        else -> reader.skipValue()
                    }
                }
            }

            if (feed.name == null) feed.name = URI.create(feed.url).host

            feeds += feed
            reader.endObject()
        }

        reader.endArray()
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of("id", "url", "title",
                "faviconLink", "folderId", "link")
    }
}