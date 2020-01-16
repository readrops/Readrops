package com.readrops.readropslibrary.localfeed.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JSONAuthor(val name: String,
                      val url: String,
                      @Json(name = "avatar") val avatarUrl: String?)