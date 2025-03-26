package com.readrops.api.services.fever.adapters

import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.toBoolean
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

class FeverAPIAdapter : JsonAdapter<Boolean>() {

    @ToJson
    override fun toJson(writer: JsonWriter, value: Boolean?) {
        // useless here
    }

    @FromJson
    override fun fromJson(reader: JsonReader): Boolean = with(reader) {
        return try {
            beginObject()

            var authenticated = false
            while (hasNext()) {
                when (nextName()) {
                    "auth" -> authenticated = nextInt().toBoolean()
                    else -> skipValue()
                }
            }

            endObject()
            authenticated
        } catch (e: Exception) {
            throw ParseException("Fever API parsing failure", e)
        }
    }
}