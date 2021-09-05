package com.readrops.api.localfeed

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.readrops.api.utils.extensions.checkRoot
import java.io.InputStream

object LocalRSSHelper {

    private const val RSS_1_CONTENT_TYPE = "application/rdf+xml"
    private const val RSS_2_CONTENT_TYPE = "application/rss+xml"
    private const val ATOM_CONTENT_TYPE = "application/atom+xml"
    private const val JSONFEED_CONTENT_TYPE = "application/feed+json"
    private const val JSON_CONTENT_TYPE = "application/json"

    const val RSS_1_ROOT_NAME = "RDF"
    const val RSS_2_ROOT_NAME = "rss"
    const val ATOM_ROOT_NAME = "feed"
    val RSS_ROOT_NAMES = Names.of(RSS_1_ROOT_NAME, RSS_2_ROOT_NAME, ATOM_ROOT_NAME)

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

    @JvmStatic
    fun isRSSType(type: String?): Boolean =
            if (type != null) getRSSType(type) != RSSType.UNKNOWN else false

    fun guessRSSType(konsumer: Konsumer): RSSType = when {
        konsumer.checkRoot(RSS_1_ROOT_NAME) -> RSSType.RSS_1
        konsumer.checkRoot(RSS_2_ROOT_NAME) -> RSSType.RSS_2
        konsumer.checkRoot(ATOM_ROOT_NAME) -> RSSType.ATOM
        else -> RSSType.UNKNOWN
    }

    enum class RSSType {
        RSS_1,
        RSS_2,
        ATOM,
        JSONFEED,
        UNKNOWN
    }
}