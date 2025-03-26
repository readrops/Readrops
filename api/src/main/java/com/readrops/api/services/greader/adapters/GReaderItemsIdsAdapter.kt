package com.readrops.api.services.greader.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class GReaderItemsIdsAdapter : JsonAdapter<List<String>>() {

    override fun toJson(writer: JsonWriter, value: List<String>?) {
        // not useful here
    }

    @SuppressLint("CheckResult")
    override fun fromJson(reader: JsonReader): List<String>? = with(reader) {
        val ids = arrayListOf<String>()

        return try {
            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "itemRefs" -> {
                        beginArray()

                        while (hasNext()) {
                            beginObject()

                            when (nextName()) {
                                "id" -> {
                                    val value = nextNonEmptyString()
                                    ids += "tag:google.com,2005:reader/item/" +
                                            value.toLong()
                                                .toString(16).padStart(value.length, '0')
                                }

                                else -> skipValue()
                            }

                            endObject()
                        }

                        endArray()
                    }
                    else -> skipValue()
                }
            }

            endObject()
            ids
        } catch (e: Exception) {
            throw ParseException("GReader items ids parsing failure", e)
        }
    }
}