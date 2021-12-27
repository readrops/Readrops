package com.readrops.api.services.fever.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.skipField
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

class FeverItemsIdsAdapter {

    @ToJson
    fun toJson(ids: List<String>) = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<String> = with(reader) {
        return try {
            beginObject()
            repeat(3) {
                skipField()
            }

            nextName() // (unread|saved)_item_ids field
            val ids = nextString().split(",")

            endObject()
            ids
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }
}