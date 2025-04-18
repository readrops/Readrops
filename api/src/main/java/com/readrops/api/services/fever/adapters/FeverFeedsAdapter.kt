package com.readrops.api.services.fever.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.db.entities.Feed
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

data class FeverFeeds(
    val feeds: List<Feed> = listOf(),
    val favicons: Map<Int, String> = mapOf(), // <faviconId, feedRemoteId>
    val feedsGroups: Map<Int, List<Int>> = emptyMap()
)

class FeverFeedsAdapter : JsonAdapter<FeverFeeds>() {

    override fun toJson(writer: JsonWriter, value: FeverFeeds?) {
        // not useful here
    }

    @SuppressLint("CheckResult")
    override fun fromJson(reader: JsonReader): FeverFeeds = with(reader) {
        return try {
            val feeds = arrayListOf<Feed>()
            val favicons = mutableMapOf<Int, String>()
            val feedsGroups = mutableMapOf<Int, List<Int>>()

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "feeds" -> {
                        beginArray()
                        while (hasNext()) {
                            beginObject()
                            feeds += parseFeed(reader, favicons)

                            endObject()
                        }

                        endArray()
                    }
                    "feeds_groups" -> {
                        beginArray()
                        while (hasNext()) {
                            beginObject()

                            val (folderId, feedsIds) = parseFeedsGroups(reader)
                            folderId?.let { feedsGroups[it] = feedsIds }

                            endObject()
                        }

                        endArray()
                    }
                    else -> skipValue()
                }
            }

            endObject()

            FeverFeeds(
                feeds = feeds,
                favicons = favicons,
                feedsGroups = feedsGroups
            )
        } catch (e: Exception) {
            throw ParseException("Fever feeds parsing failure", e)
        }
    }

    private fun parseFeed(reader: JsonReader, favicons: MutableMap<Int, String>): Feed = with(reader) {
        val feed = Feed()
        while (hasNext()) {
            with(feed) {
                when (selectName(NAMES)) {
                    0 -> remoteId = nextInt().toString()
                    1 -> favicons[nextInt()] = remoteId!!
                    2 -> name = nextNonEmptyString()
                    3 -> url = nextNonEmptyString()
                    4 -> siteUrl = nextNullableString()
                    else -> skipValue()
                }
            }
        }

        return feed
    }

    private fun parseFeedsGroups(reader: JsonReader): Pair<Int?, List<Int>> = with(reader) {
        var folderId: Int? = null
        val feedsIds = mutableListOf<Int>()

        while (hasNext()) {
            when (selectName(JsonReader.Options.of("group_id", "feed_ids"))) {
                0 -> folderId = nextInt()
                1 -> feedsIds += nextNonEmptyString().split(",").map { it.toInt() }
                else -> skipValue()
            }
        }

        folderId to feedsIds
    }

    companion object {
        val NAMES: JsonReader.Options =
            JsonReader.Options.of("id", "favicon_id", "title", "url", "site_url")
    }
}