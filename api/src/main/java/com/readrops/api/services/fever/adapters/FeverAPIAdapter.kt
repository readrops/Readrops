package com.readrops.api.services.fever.adapters

import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.skipField
import com.squareup.moshi.*

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
            }

            skipField()
            endObject()


            authenticated == 1
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }
}