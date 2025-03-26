package com.readrops.api.services.greader.adapters

import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNullableString
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

data class FreshRSSUserInfo(
        val userName: String?,
)

class FreshRSSUserInfoAdapter {

    @ToJson
    fun toJson(userInfo: FreshRSSUserInfo) = ""

    @FromJson
    fun fromJson(reader: JsonReader): FreshRSSUserInfo = with(reader) {
        var userName: String? = null

        return try {
            beginObject()

            while (hasNext()) {
                when (nextName()) {
                    "userName" -> userName = nextNullableString()
                    else -> skipValue()
                }
            }

            endObject()
            FreshRSSUserInfo(userName)
        } catch (e: Exception) {
            throw ParseException("GReader user info parsing failure", e)
        }
    }
}
