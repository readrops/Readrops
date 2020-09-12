package com.readrops.api.localfeed

import com.readrops.api.utils.UnknownFormatException
import java.io.InputStream
import java.util.regex.Pattern

object LocalRSSHelper {

    private const val RSS_DEFAULT_CONTENT_TYPE = "application/rss+xml"
    private const val RSS_TEXT_CONTENT_TYPE = "text/xml"
    private const val RSS_APPLICATION_CONTENT_TYPE = "application/xml"
    private const val ATOM_CONTENT_TYPE = "application/atom+xml"
    private const val JSON_CONTENT_TYPE = "application/json"
    private const val HTML_CONTENT_TYPE = "text/html"

    private const val RSS_2_REGEX = "rss.*version=\"2.0\""

    private const val ATOM_REGEX = "<feed.* xmlns=\"http://www.w3.org/2005/Atom\""

    /**
     * Guess RSS type based on content-type header
     */
    fun getRSSType(contentType: String): RSSType {
        return when (contentType) {
            RSS_DEFAULT_CONTENT_TYPE -> RSSType.RSS_2
            ATOM_CONTENT_TYPE -> RSSType.ATOM
            JSON_CONTENT_TYPE -> RSSType.JSONFEED
            RSS_TEXT_CONTENT_TYPE, RSS_APPLICATION_CONTENT_TYPE, HTML_CONTENT_TYPE -> RSSType.UNKNOWN
            else -> throw UnknownFormatException("Unknown content type : $contentType")
        }
    }

    /**
     * Guess RSS type based on xml content
     */
    fun getRSSContentType(content: InputStream): RSSType {
        val stringBuffer = StringBuffer()
        val reader = content.bufferedReader()

        var currentLine = reader.readLine()
        while (currentLine != null) {
            stringBuffer.append(currentLine)

            if (Pattern.compile(RSS_2_REGEX).matcher(stringBuffer.toString()).find()) {
                reader.close()
                content.close()

                return RSSType.RSS_2
            } else if (Pattern.compile(ATOM_REGEX).matcher(stringBuffer.toString()).find()) {
                reader.close()
                content.close()

                return RSSType.ATOM
            }

            currentLine = reader.readLine()
        }

        return RSSType.UNKNOWN
    }

    enum class RSSType {
        RSS_2,
        ATOM,
        JSONFEED,
        UNKNOWN
    }
}