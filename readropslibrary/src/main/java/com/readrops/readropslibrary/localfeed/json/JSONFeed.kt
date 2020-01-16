package com.readrops.readropslibrary.localfeed.json

import com.readrops.readropslibrary.localfeed.AFeed
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JSONFeed(val version: String,
                    val title: String,
                    @Json(name = "home_page_url") val homePageUrl: String?,
                    @Json(name = "feed_url") val feedUrl: String?,
                    val description: String?,
                    @Json(name = "icon") val iconUrl: String?,
                    @Json(name = "favicon") val faviconUrl: String?,
                    val items: List<JSONItem>) : AFeed()