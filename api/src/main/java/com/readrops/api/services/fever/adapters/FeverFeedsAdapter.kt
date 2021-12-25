package com.readrops.api.services.fever.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.api.utils.extensions.nextNullableString
import com.readrops.api.utils.extensions.skipField
import com.readrops.api.utils.extensions.skipToEnd
import com.readrops.db.entities.Feed
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

class FeverFeedsAdapter {

    @ToJson
    fun toJson(feeds: List<Feed>) = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<Feed> = with(reader) {
        return try {
            val feeds = arrayListOf<Feed>()

            beginObject()

            // skip basic fields (api_version, auth, last_refreshed...)
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
                        when(selectName(NAMES)) {
                            0 -> remoteId = nextInt().toString()
                            1 -> name = nextNonEmptyString()
                            2 -> url = nextNonEmptyString()
                            3 -> siteUrl = nextNullableString()
                            else -> skipValue()
                        }
                    }
                }

                feeds += feed
                endObject()
            }

            endArray()
            skipToEnd()

            feeds
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of("id", "title", "url", "site_url")
    }
}