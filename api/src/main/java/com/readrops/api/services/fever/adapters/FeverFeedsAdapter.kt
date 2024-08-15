package com.readrops.api.services.fever.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.api.utils.extensions.skipField
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

            // skip based fields (api_version, auth, last_refreshed...)
            repeat(3) {
                skipField()
            }

            nextName() // beginning of feeds array
            beginArray()

            while (hasNext()) {
                beginObject()

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

                feeds += feed
                endObject()
            }

            endArray()

            nextName()
            beginArray()

            while (hasNext()) {
                beginObject()

                var folderId: Int? = null
                val feedsIds = mutableListOf<Int>()
                while (hasNext()) {
                    when (selectName(JsonReader.Options.of("group_id", "feed_ids"))) {
                        0 -> folderId = nextInt()
                        1 -> feedsIds += nextNonEmptyString().split(",").map { it.toInt() }
                        else -> skipValue()
                    }
                }

                folderId?.let { feedsGroups[it] = feedsIds }
                endObject()
            }

            endArray()
            endObject()

            FeverFeeds(
                feeds = feeds,
                favicons = favicons,
                feedsGroups = feedsGroups
            )
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    companion object {
        val NAMES: JsonReader.Options =
            JsonReader.Options.of("id", "favicon_id", "title", "url", "site_url")
    }
}