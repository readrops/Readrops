package com.readrops.api.localfeed

import java.io.InputStream
import java.util.regex.Pattern

object LocalRSSHelper {

    private const val RSS_1_CONTENT_TYPE = "application/rdf+xml"
    private const val RSS_2_CONTENT_TYPE = "application/rss+xml"
    private const val ATOM_CONTENT_TYPE = "application/atom+xml"
    private const val JSONFEED_CONTENT_TYPE = "application/feed+json"
    private const val JSON_CONTENT_TYPE = "application/json"

    private const val RSS_2_REGEX = "rss.*version=\"2.0\""

    private const val ATOM_REGEX = "<feed.* xmlns=\"http://www.w3.org/2005/Atom\""

    /**
     * Guess RSS type based on content-type header
     */
    fun getRSSType(contentType: String): RSSType {
        return when (contentType) {
            RSS_1_CONTENT_TYPE -> RSSType.RSS_1
            RSS_2_CONTENT_TYPE -> RSSType.RSS_2
            ATOM_CONTENT_TYPE -> RSSType.ATOM
            JSON_CONTENT_TYPE, JSONFEED_CONTENT_TYPE -> RSSType.JSONFEED
            else -> RSSType.UNKNOWN
        }
    }

    /**
     * Guess RSS type based on xml content
     */
    fun getRSSContentType(content: InputStream): RSSType {
        val stringBuffer = StringBuffer()
        val reader = content.bufferedReader()
        var type = RSSType.UNKNOWN

        // we get the first 10 lines which should be sufficient to get the type,
        // otherwise iterating over the whole file could be too slow
        for (i in 0..9) stringBuffer.append(reader.readLine())

        if (Pattern.compile(RSS_2_REGEX).matcher(stringBuffer.toString()).find()) {
            type = RSSType.RSS_2
        } else if (Pattern.compile(ATOM_REGEX).matcher(stringBuffer.toString()).find()) {
            type = RSSType.ATOM
        }

        reader.close()
        content.close()
        return type
    }

    enum class RSSType {
        RSS_1,
        RSS_2,
        ATOM,
        JSONFEED,
        UNKNOWN
    }
}