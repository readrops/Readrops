package com.readrops.api.localfeed.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JSONItem(val id: String,
                    val title: String?,
                    val summary: String?,
                    @Json(name = "content_text") val contentText: String?,
                    @Json(name = "content_html") val contentHtml: String?,
                    val url: String?,
                    @Json(name = "image") val imageUrl: String?,
                    @Json(name = "date_published") val pubDate: String,
                    @Json(name = "date_modified") val modDate: String?,
                    val author: JSONAuthor?) {

    fun getContent(): String? {
        return contentHtml ?: contentText
    }
}