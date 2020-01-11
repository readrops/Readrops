package com.readrops.readropslibrary.services.nextcloudnews.json

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NextNewsUser(val userId: String,
                        val displayName: String,
                        val lastLoginTimestamp: Long,
                        val avatar: Avatar?) {

    @JsonClass(generateAdapter = true)
    data class Avatar(val data: String,
                      val mime: String)
}