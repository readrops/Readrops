package com.readrops.api.services.fever.adapters

import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.skipField
import com.readrops.api.utils.extensions.toBoolean
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
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
            skipField()

            var authenticated = 0
            if (nextName() == "auth") {
                authenticated = nextInt()
            } else {
                skipValue()
            }

            while (peek() == Token.NAME) {
                skipField()
            }

            endObject()
            authenticated.toBoolean()
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }
}