package com.readrops.api.localfeed

import android.accounts.NetworkErrorException
import androidx.annotation.WorkerThread
import com.readrops.api.localfeed.json.JSONFeedAdapter
import com.readrops.api.localfeed.json.JSONItemsAdapter
import com.readrops.api.utils.LibUtils
import com.readrops.api.utils.ParseException
import com.readrops.api.utils.UnknownFormatException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

class LocalRSSDataSource(private val httpClient: OkHttpClient) {

    /**
     * Query RSS url
     * @param url url to query
     * @param headers request headers
     * @return a Feed object with its items
     */
    @Throws(ParseException::class, UnknownFormatException::class, NetworkErrorException::class, IOException::class)
    @WorkerThread
    fun queryRSSResource(url: String, headers: Headers?): Pair<Feed, List<Item>>? {
        val response = queryUrl(url, headers)

        return when {
            response.isSuccessful -> {
                val header = response.header(LibUtils.CONTENT_TYPE_HEADER)
                        ?: throw UnknownFormatException("Unable to get $url content-type")

                val contentType = LibUtils.parseContentType(header)
                        ?: throw ParseException("Unable to parse $url content-type")

                var type = LocalRSSHelper.getRSSType(contentType)

                val bodyArray = response.peekBody(Long.MAX_VALUE).bytes()

                // if we can't guess type based on content-type header, we use the content
                if (type == LocalRSSHelper.RSSType.UNKNOWN)
                    type = LocalRSSHelper.getRSSContentType(ByteArrayInputStream(bodyArray))
                // if we can't guess type even with the content, we are unable to go further
                if (type == LocalRSSHelper.RSSType.UNKNOWN) throw UnknownFormatException("Unable to guess $url RSS type")

                val feed = parseFeed(ByteArrayInputStream(bodyArray), type, response)
                val items = parseItems(ByteArrayInputStream(bodyArray), type)

                response.body?.close()
                Pair(feed, items)
            }
            response.code == HttpURLConnection.HTTP_NOT_MODIFIED -> null
            else -> throw NetworkErrorException("$url returned ${response.code} code : ${response.message}")
        }
    }

    /**
     * Checks if the provided url is a RSS resource
     * @param url url to check
     * @return true if [url] is a RSS resource, false otherwise
     */
    @WorkerThread
    fun isUrlRSSResource(url: String): Boolean {
        val response = queryUrl(url, null)

        return if (response.isSuccessful) {
            val header = response.header(LibUtils.CONTENT_TYPE_HEADER)
                    ?: return false

            val contentType = LibUtils.parseContentType(header)
                    ?: return false

            var type = LocalRSSHelper.getRSSType(contentType)

            if (type == LocalRSSHelper.RSSType.UNKNOWN)
                type = LocalRSSHelper.getRSSContentType(response.body?.byteStream()!!) // stream is closed in helper method

            type != LocalRSSHelper.RSSType.UNKNOWN
        } else false
    }

    @Throws(IOException::class)
    private fun queryUrl(url: String, headers: Headers?): Response {
        val requestBuilder = Request.Builder().url(url)
        headers?.let { requestBuilder.headers(it) }

        return httpClient.newCall(requestBuilder.build()).execute()
    }

    private fun parseFeed(stream: InputStream, type: LocalRSSHelper.RSSType, response: Response): Feed {
        val feed = if (type != LocalRSSHelper.RSSType.JSONFEED) {
            val adapter = XmlAdapter.xmlFeedAdapterFactory(type)

            adapter.fromXml(stream)
        } else {
            val adapter = Moshi.Builder()
                    .add(JSONFeedAdapter())
                    .build()
                    .adapter(Feed::class.java)

            adapter.fromJson(Buffer().readFrom(stream))!!
        }

        handleSpecialCases(feed, type, response)

        feed.etag = response.header(LibUtils.ETAG_HEADER)
        feed.lastModified = response.header(LibUtils.LAST_MODIFIED_HEADER)

        return feed
    }

    private fun parseItems(stream: InputStream, type: LocalRSSHelper.RSSType): List<Item> {
        return if (type != LocalRSSHelper.RSSType.JSONFEED) {
            val adapter = XmlAdapter.xmlItemsAdapterFactory(type)

            adapter.fromXml(stream)
        } else {
            val adapter = Moshi.Builder()
                    .add(Types.newParameterizedType(MutableList::class.java, Item::class.java), JSONItemsAdapter())
                    .build()
                    .adapter<List<Item>>(Types.newParameterizedType(MutableList::class.java, Item::class.java))

            adapter.fromJson(Buffer().readFrom(stream))!!
        }
    }

    private fun handleSpecialCases(feed: Feed, type: LocalRSSHelper.RSSType, response: Response) {
        with(feed) {
            if (type == LocalRSSHelper.RSSType.RSS_2) {
                // if an atom:link element was parsed, we still replace its value as it is unreliable,
                // otherwise we just add the rss url
                url = response.request.url.toString()
            } else if (type == LocalRSSHelper.RSSType.ATOM || type == LocalRSSHelper.RSSType.RSS_1) {
                if (url == null) url = response.request.url.toString()
                if (siteUrl == null) siteUrl = response.request.url.scheme + "://" + response.request.url.host
            }
        }
    }
}