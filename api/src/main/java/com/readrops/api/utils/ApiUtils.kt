package com.readrops.api.utils

import org.jsoup.Jsoup
import java.util.regex.Pattern

object ApiUtils {
    const val HTML_CONTENT_TYPE = "text/html"

    const val CONTENT_TYPE_HEADER = "content-type"
    const val ETAG_HEADER = "ETag"
    const val IF_NONE_MATCH_HEADER = "If-None-Match"
    const val LAST_MODIFIED_HEADER = "Last-Modified"
    const val IF_MODIFIED_HEADER = "If-Modified-Since"

    const val HTTP_UNPROCESSABLE = 422
    const val HTTP_NOT_FOUND = 404
    const val HTTP_CONFLICT = 409

    private const val RSS_CONTENT_TYPE_REGEX = "([^;]+)"

    fun isMimeImage(type: String): Boolean =
            type == "image" || type == "image/jpeg" || type == "image/jpg" || type == "image/png"

    fun parseContentType(header: String?): String? {
        val matcher = Pattern.compile(RSS_CONTENT_TYPE_REGEX)
                .matcher(header)
        return if (matcher.find()) {
            matcher.group(0)
        } else {
            null
        }
    }

    /**
     * Remove html tags and trim the text
     *
     * @param text string to clean
     * @return cleaned text
     */
    fun cleanText(text: String?): String {
        return Jsoup.parse(text).text().trim()
    }
}