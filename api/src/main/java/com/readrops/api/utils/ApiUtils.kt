package com.readrops.api.utils

import okhttp3.MediaType
import org.jsoup.Jsoup
import java.math.BigInteger
import java.security.MessageDigest
import java.util.regex.Pattern

object ApiUtils {
    val MediaType.isHtml: Boolean
        get() = type == "text" && subtype == "html"

    const val CONTENT_TYPE_HEADER = "content-type"
    const val ETAG_HEADER = "ETag"
    const val IF_NONE_MATCH_HEADER = "If-None-Match"
    const val LAST_MODIFIED_HEADER = "Last-Modified"
    const val IF_MODIFIED_HEADER = "If-Modified-Since"

    val OPML_MIMETYPES = listOf("application/xml", "text/xml", "text/x-opml", "application/octet-stream")

    private const val RSS_CONTENT_TYPE_REGEX = "([^;]+)"

    fun isMimeImage(type: String): Boolean =
            type == "image" || type == "image/jpeg" || type == "image/jpg" || type == "image/png"

    fun parseContentType(header: String): String? {
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
    fun cleanText(text: String): String {
        return Jsoup.parse(text).text().trim()
    }

    fun md5hash(value: String): String {
        val bytes = MessageDigest.getInstance("MD5")
                .digest(value.toByteArray())

        return BigInteger(1, bytes).toString(16)
    }
}