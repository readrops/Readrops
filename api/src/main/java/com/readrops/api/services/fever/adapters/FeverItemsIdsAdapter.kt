package com.readrops.api.services.fever.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
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

            val ids = arrayListOf<String>()
            while (hasNext()) {
                when (nextName()) {
                    "unread_item_ids" -> ids.addAll(nextString().split(","))
                    else -> skipValue()
                }
            }

            endObject()
            ids
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }
}